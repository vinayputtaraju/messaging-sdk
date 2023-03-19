package vinay.messagingsdk.test;

import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vinay.messagingsdk.channel.JMSRespondingService;
import vinay.messagingsdk.dto.MessageBody;
import vinay.messagingsdk.dto.MessageResponse;

@Service(value = "sample2JMSRespondingService")
@Slf4j
public class Sample2JMSRespondingService extends JMSRespondingService {

    @Override
    public MessageResponse respondMessage(Message message, MessageBody messageBody) {
        log.info("Message Received {}", messageBody);
        return MessageResponse.builder().responseBody("Success JMS operation : " + messageBody.getRequestBody())
                .success(true).build();
    }
}
