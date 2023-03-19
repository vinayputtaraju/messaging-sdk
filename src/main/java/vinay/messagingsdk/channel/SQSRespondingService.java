package vinay.messagingsdk.channel;

import software.amazon.awssdk.services.sqs.model.Message;
import vinay.messagingsdk.dto.MessageBody;
import vinay.messagingsdk.dto.MessageResponse;

public interface SQSRespondingService {

    public MessageResponse respondMessage(Message message, MessageBody messageBody);

    default void handleException(Exception e){

    }
}
