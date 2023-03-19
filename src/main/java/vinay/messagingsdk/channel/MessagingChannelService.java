package vinay.messagingsdk.channel;


import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;

public interface MessagingChannelService {

    MessageResponse sendAndReceiveMessage(MessageRequest messageRequest) throws Exception;
}
