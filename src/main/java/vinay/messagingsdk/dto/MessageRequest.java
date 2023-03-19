package vinay.messagingsdk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    String channelName;
    Integer timeout;
    MessageBody messageBody;
    Integer delayMessageInSeconds;
    Map<String, String> messageAttributes;
}
