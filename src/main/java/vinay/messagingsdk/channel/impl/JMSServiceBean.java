package vinay.messagingsdk.channel.impl;

import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import vinay.messagingsdk.channel.MessagingChannelService;
import vinay.messagingsdk.config.MessagingConfig;
import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;
import vinay.messagingsdk.util.Utility;

@Service(value = "JMSService")
@ConditionalOnProperty(prefix = "messaging", name = "channel", havingValue = "JMSService")
@Slf4j
public class JMSServiceBean implements MessagingChannelService {

    private JmsTemplate jmsTemplate;

    private MappingJackson2MessageConverter messageConverter;

    MessagingConfig messagingConfig;

    public JMSServiceBean(JmsTemplate jmsTemplate,
                          final MessagingConfig messagingConfig) {
        if (CollectionUtils.isEmpty(messagingConfig.getJmsQueues())) {
            log.warn("No JMS queues configured");
        }

        this.jmsTemplate = jmsTemplate;
        this.jmsTemplate.setDeliveryPersistent(false);
        this.jmsTemplate.setReceiveTimeout(messagingConfig.getDefaultReceiveTimeout());
        this.messagingConfig = messagingConfig;
        messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        messageConverter.setTypeIdPropertyName("_type");
    }

    @Override
    public MessageResponse sendAndReceiveMessage(MessageRequest messageRequest) throws Exception {
        MessagingConfig.JMSConfig jmsConfig = messagingConfig.getJmsQueues().get(messageRequest.getChannelName());

        if (null != messageRequest.getTimeout()) {
            jmsTemplate.setReceiveTimeout(messageRequest.getTimeout());
        }
        if (null != messageRequest.getDelayMessageInSeconds()) {
            jmsTemplate.setDeliveryDelay(messageRequest.getDelayMessageInSeconds());
        }
        Message response = jmsTemplate.sendAndReceive(jmsConfig.getDestinationQueueName(), session -> {
            Message request = messageConverter.toMessage(messageRequest.getMessageBody(), session);
            if (!ObjectUtils.isEmpty(messageRequest.getMessageBody().getCorrelationId()))
                request.setJMSCorrelationID(messageRequest.getMessageBody().getCorrelationId());

            return request;
        });
        return Utility.OBJECT_MAPPER.readValue(response.getBody(String.class), MessageResponse.class);
    }
}
