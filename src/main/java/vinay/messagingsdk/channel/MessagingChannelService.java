package vinay.messagingsdk.channel;


import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;

public interface MessagingChannelService {

    MessageResponse sendAndReceiveMessage(final MessageRequest messageRequest) throws Exception;
}
