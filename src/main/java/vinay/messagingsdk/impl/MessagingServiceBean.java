package vinay.messagingsdk.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import vinay.messagingsdk.MessagingService;
import vinay.messagingsdk.channel.MessagingChannelService;
import vinay.messagingsdk.config.MessagingConfig;
import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;
import vinay.messagingsdk.exception.MessagingException;

@Service
@Slf4j
public class MessagingServiceBean implements MessagingService {

    @Autowired
    MessagingConfig messagingConfig;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public MessageResponse sendAndReceiveMessage(final MessageRequest messageRequest) throws MessagingException {
        if(ObjectUtils.isEmpty(messagingConfig.getChannel()))
            throw new MessagingException("Messaging channel not defined");
        try {
            MessagingChannelService messagingChannelService=applicationContext
                    .getBean(messagingConfig.getChannel().getServiceName(),MessagingChannelService.class);
            return messagingChannelService.sendAndReceiveMessage(messageRequest);
        } catch (Exception e) {
            log.error("error", e);
            throw new MessagingException("error while sending message", e);
        }
    }
}
