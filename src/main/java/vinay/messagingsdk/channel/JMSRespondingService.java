package vinay.messagingsdk.channel;

import jakarta.jms.Message;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import vinay.messagingsdk.dto.MessageBody;
import vinay.messagingsdk.dto.MessageResponse;
import vinay.messagingsdk.util.Utility;


@Slf4j
public abstract class JMSRespondingService extends AbstractAdaptableMessageListener {

    private MappingJackson2MessageConverter messageConverter;

    public JMSRespondingService() {
        messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTargetType(MessageType.TEXT);
        messageConverter.setTypeIdPropertyName("_type");
    }

    @Override
    public void onMessage(Message message, Session session) {
        super.setMessageConverter(this.messageConverter);
        try {
            MessageBody messageBody = Utility.OBJECT_MAPPER.readValue(message.getBody(String.class)
                    , MessageBody.class);
            MessageResponse messageResponse = this.respondMessage(message, messageBody);
            handleResult(messageResponse, message, session);
        } catch (Exception e) {
            log.error("error while receiving message", e);
        }

    }

    public abstract MessageResponse respondMessage(Message message, MessageBody messageBody);

    void handleException(Exception e) {
    }
}
