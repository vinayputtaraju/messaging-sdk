package vinay.messagingsdk.channel.impl;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public JMSServiceBean(final MessagingConfig messagingConfig, @Autowired(required = false) ConnectionFactory connectionFactory) {
        if (CollectionUtils.isEmpty(messagingConfig.getJmsQueues())) {
            log.warn("No JMS queues configured");
        }
        if(!messagingConfig.getJmsQueues().values().stream().anyMatch(MessagingConfig.JMSConfig::isSendMessage)){
            log.info("not configured for sending message");
            return;
        }
        JmsPoolConnectionFactory jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
        jmsPoolConnectionFactory.setMaxConnections(messagingConfig.getMaxConnectionsInPool());
        jmsPoolConnectionFactory.setConnectionFactory(connectionFactory);
        this.jmsTemplate = new JmsTemplate(jmsPoolConnectionFactory);
        this.jmsTemplate.setReceiveTimeout(messagingConfig.getDefaultReceiveTimeout() * 1000);

        messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        messageConverter.setTypeIdPropertyName("_type");
    }

    @Override
    public MessageResponse sendAndReceiveMessage(MessageRequest messageRequest) throws Exception {
        MessagingConfig.JMSConfig jmsConfig = messagingConfig.getJmsQueues().get(messageRequest.getChannelName());

        if (null != messageRequest.getTimeout()) {
            jmsTemplate.setReceiveTimeout(messageRequest.getTimeout() * 1000);
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
