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
    private Channel channel;
    private Map<String, SQSConfig> sqsQueues = new HashMap<>();
    int defaultReceiveTimeout = 10;
    private String awsRegion = "us-east-1";
    private long awsConnectTimeout = 60l;
    private long awsSocketTimeout = 60l;

    private int maxConnectionsInPool = 5;

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

    public enum Channel {
        SQS("SQSService");
        private String serviceName;

        Channel(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return this.serviceName;
        }
    }
}
