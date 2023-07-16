package vinay.messagingsdk;

import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;
import vinay.messagingsdk.exception.MessagingException;

public interface MessagingService {

    MessageResponse sendAndReceiveMessage(final MessageRequest messageRequest) throws MessagingException;
}
