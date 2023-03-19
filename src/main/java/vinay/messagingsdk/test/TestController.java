package vinay.messagingsdk.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vinay.messagingsdk.MessagingService;
import vinay.messagingsdk.dto.MessageBody;
import vinay.messagingsdk.dto.MessageRequest;
import vinay.messagingsdk.dto.MessageResponse;

@RestController
@RequestMapping(value = "/msg")
@Slf4j
public class TestController {

    @Resource
    MessagingService messagingService;

    @GetMapping("/send/{message}")
    public MessageResponse test(@PathVariable("message") String message) throws Exception {
        return messagingService.sendAndReceiveMessage(MessageRequest.builder()
                .channelName(message).messageBody(MessageBody.builder()
                        .requestBody(message).build())
                .build());
    }
}
