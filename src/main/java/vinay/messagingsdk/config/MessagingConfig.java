package vinay.messagingsdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "messaging")
public class MessagingConfig {

    private String channel;
    private Map<String, SQSConfig> sqsQueues = new HashMap<>();
    private Map<String, JMSConfig> jmsQueues = new HashMap<>();
    int defaultReceiveTimeout = 10;
    private String awsRegion = "us-east-1";
    private long awsConnectTimeout = 60l;
    private long awsSocketTimeout = 60l;

    @Data
    public static class SQSConfig {
        String prefix;
        String queueUrl;
        String dlqArn;
        String maxReceiveCount = "1";
        Integer queueHeartBeatInterval = 360;
        Integer pollingThreadCount = 5;
        Integer idleQueueRetentionPeriod = 300;
        Integer idleQueueSweepingPeriod = 300;
        boolean sendMessage;
        boolean respondMessage;
    }

    @Data
    public static class JMSConfig {
        String destinationQueueName;
        boolean sendMessage;
        boolean respondMessage;
    }
}
