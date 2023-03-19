package vinay.messagingsdk.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import vinay.messagingsdk.channel.JMSRespondingService;

@Configuration
@ConditionalOnProperty(prefix = "messaging", name = "channel", havingValue = "JMSService")
@EnableJms
public class JMSListenerConfig implements JmsListenerConfigurer {

    final MessagingConfig messagingConfig;
    final ApplicationContext applicationContext;

    public JMSListenerConfig(MessagingConfig messagingConfig, ApplicationContext applicationContext) {
        this.messagingConfig = messagingConfig;
        this.applicationContext = applicationContext;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        messagingConfig.getJmsQueues().forEach((k, v) -> {
            if (!v.isRespondMessage())
                return;
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId(k);
            endpoint.setDestination(v.getDestinationQueueName());
            JMSRespondingService jmsRespondingService = this.applicationContext.getBean(k + "JMSRespondingService", JMSRespondingService.class);
            endpoint.setMessageListener(jmsRespondingService);
            registrar.registerEndpoint(endpoint);
        });
    }
}
