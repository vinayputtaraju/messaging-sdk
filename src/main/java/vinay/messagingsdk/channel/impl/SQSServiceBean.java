package vinay.messagingsdk.channel.impl;

import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.util.SQSMessageConsumer;
import com.amazonaws.services.sqs.util.SQSMessageConsumerBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import vinay.messagingsdk.channel.MessagingChannelService;
import vinay.messagingsdk.channel.SQSRespondingService;
import vinay.messagingsdk.config.MessagingConfig;
import vinay.messagingsdk.dto.MessageBody;
import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;
import vinay.messagingsdk.util.Utility;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service(value = "SQSService")
@ConditionalOnProperty(prefix = "messaging", name = "channel", havingValue = "SQSService")
@Slf4j
public class SQSServiceBean implements MessagingChannelService {
    private static final String CORRELATION_ID_PARAM = "correlationId";

    Map<String, AmazonSQSRequester> sqsRequesters;
    Map<String, AmazonSQSResponder> sqsResponders;
    Map<String, SQSMessageConsumer> sqsMessageConsumers;
    MessagingConfig messagingConfig;
    ApplicationContext applicationContext;
    SqsClient sqsClient;

    public SQSServiceBean(ApplicationContext applicationContext, MessagingConfig messagingConfig) {
        if (CollectionUtils.isEmpty(messagingConfig.getSqsQueues())) {
            log.warn("No SQS queues configured");
        }
        Region region = Region.of(messagingConfig.getAwsRegion());
        SdkHttpClient.Builder httpBuilder = ApacheHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(messagingConfig.getAwsConnectTimeout()))
                .socketTimeout(Duration.ofSeconds(messagingConfig.getAwsSocketTimeout()));
        this.sqsClient = SqsClient.builder().region(region).httpClient(httpBuilder.build())
                .credentialsProvider(DefaultCredentialsProvider.create()).build();
        this.messagingConfig = messagingConfig;
        this.applicationContext = applicationContext;
        this.sqsRequesters = new HashMap<>();
        this.sqsResponders = new HashMap<>();
        this.sqsMessageConsumers = new HashMap<>();
        messagingConfig.getSqsQueues().forEach((k, v) -> {
            if (v.isSendMessage()) {
                Map<String, String> map = new HashMap<>() {{
                    put("RedrivePolicy", "{\"maxReceiveCount\":" + v.getMaxReceiveCount() +
                            ",\"deadLetterTargetArn\":\"" + v.getDlqArn() + "\"}");
                }};
                sqsRequesters.put(k, AmazonSQSRequesterClientBuilder.standard().withAmazonSQS(sqsClient)
                        .withInternalQueuePrefix(v.getPrefix()).withQueueHeartbeatInterval(v.getQueueHeartBeatInterval())
                        .withIdleQueueRetentionPeriodSeconds(v.getIdleQueueRetentionPeriod())
                        .withIdleQueueSweepingPeriod(v.getIdleQueueSweepingPeriod(), TimeUnit.SECONDS)
                        .withQueueAttributes(map)
                        .build());
                log.info("RedrivePolicy : {}", map.get("RedrivePolicy"));
            }
            if (v.isRespondMessage()) {
                sqsResponders.put(k, AmazonSQSResponderClientBuilder.standard().withAmazonSQS(sqsClient)
                        .withInternalQueuePrefix(v.getPrefix()).build());
                SQSMessageConsumer sqsMessageConsumer = SQSMessageConsumerBuilder.standard().withAmazonSQS(sqsClient)
                        .withQueueUrl(v.getQueueUrl()).withPollingThreadCount(v.getPollingThreadCount())
                        .withConsumer(message -> this.respondMessage(message, k))
                        .withMaxWaitTimeSeconds(30)
                        .withExceptionHandler(e -> this.exceptionHandler(e, k)).build();
                sqsMessageConsumers.put(k, sqsMessageConsumer);
                sqsMessageConsumer.start();
            }
        });

    }

    @PreDestroy
    public void stop() {
        sqsRequesters.forEach((k, v) -> v.shutdown());
        sqsMessageConsumers.forEach((k, v) -> v.shutdown());
    }

    @Override
    public MessageResponse sendAndReceiveMessage(MessageRequest messageRequest) throws Exception {
        int timeout = messageRequest.getTimeout();
        if (null != messageRequest.getTimeout()) {
            timeout = messageRequest.getTimeout();
        }
        Message message = sqsRequesters.get(messageRequest.getChannelName())
                .sendMessageAndGetResponse(buildSendMessageRequest(messageRequest).build(),
                        timeout, TimeUnit.SECONDS);
        log.info("Message Response : {}", message.toString());
        return Utility.OBJECT_MAPPER.readValue(message.body(), MessageResponse.class);
    }

    private void respondMessage(Message message, String channelName) {
        try {
            MessageBody messageBody = Utility.OBJECT_MAPPER.readValue(message.body(), MessageBody.class);
            log.info("Message Received : {}", messageBody);
            MessageResponse messageResponse = sqsRespondingService(channelName).respondMessage(message, messageBody);
            this.sqsResponders.get(channelName).sendResponseMessage(MessageContent.fromMessage(message),
                    new MessageContent(Utility.OBJECT_MAPPER.writeValueAsString(messageResponse)));
        } catch (Exception e) {
            log.error("error while responding message", e);
        }
    }

    private void exceptionHandler(Exception e, String channelName) {
        log.error("error", e);
        try {
            sqsRespondingService(channelName).handleException(e);
        } catch (Exception ex) {
            log.error("error", ex);
        }
    }

    private SQSRespondingService sqsRespondingService(String channelName) {
        return this.applicationContext.getBean(channelName + "RespondingService", SQSRespondingService.class);
    }

    private SendMessageRequest.Builder buildSendMessageRequest(MessageRequest messageRequest) throws Exception {
        String messageBody = Utility.OBJECT_MAPPER.writeValueAsString(messageRequest.getMessageBody());
        SendMessageRequest.Builder builder = SendMessageRequest.builder().messageBody(messageBody)
                .queueUrl(messagingConfig.getSqsQueues().get(messageRequest.getChannelName()).getQueueUrl());
        if (!ObjectUtils.isEmpty(messageRequest.getDelayMessageInSeconds()))
            builder.delaySeconds(messageRequest.getDelayMessageInSeconds());

        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();

        if (!CollectionUtils.isEmpty(messageRequest.getMessageAttributes())) {
            messageRequest.getMessageAttributes().forEach((k, v) ->
                    messageAttributes.put(k, MessageAttributeValue.builder().dataType("String").stringValue(v).build()));
        }

        if (!ObjectUtils.isEmpty(messageRequest.getMessageBody().getCorrelationId())) {
            messageRequest.getMessageAttributes().forEach((k, v) ->
                    messageAttributes.put(CORRELATION_ID_PARAM, MessageAttributeValue.builder().dataType("String").stringValue(v).build()));
        }
        if (!CollectionUtils.isEmpty(messageAttributes)) {
            builder.messageAttributes(messageAttributes);
        }
        return builder;
    }
}
