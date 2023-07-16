# messaging-sdk

SDK that can be used for message driven services with request-reply pattern.

Usage

### Dependency
```xml
<dependency>
    <groupId>vinay</groupId>
    <artifactId>messaging-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

### SQS 

Working with SQS Temporary Queue

<img src="sqs-temporary-queue.png" width="40%" height="30%">

Application Config usage

```yaml
messaging:
  channel: SQS
  defaultReceiveTimeout: 10 # wait timeperiod for the response. default is 10 seconds.
  awsRegion: "us-east-1" # default is us-east-1
  awsConnectTimeout: 60 # default is 60 seconds
  awsSocketTimeout: 60 # default is 60 seconds
  maxConnectionsInPool: 5 # Max connections in the pool to be reused. default is 5
  sqsQueues:
    sample1: # Channel name <Required>
      prefix: "sample-standard" # queue prefix <Required>
      dlqArn: "arn:aws:sqs:us-east-1:${AWS_ACCOUNT_ID}:sample-standard_DLQ" # <Required>
      queueUrl: "https://sqs.us-east-1.amazonaws.com/${AWS_ACCOUNT_ID}/sample-standard" # <Required>
      sendMessage: true # if service is a producer/client then set it as true to send message to SQS. default is false
      respondMessage: false # if service is a consumer then set it as true to poll and consume message to SQS. default is false
      pollingThreadCount: 5 # Number of threads to be used for polling messages parallely. default is 5
      idleQueueSweepingPeriod: 300 # Idle queue sweeping period for temporary queues in seconds. default is 300
      idleQueueRetentionPeriod: 300 # Idle queue retention period of temporary queues in seconds. default is 300
      queueHeartBeatInterval: 360 # to check if queue is idle for temporary queue in seconds. default is 360
      maxReceiveCount: 1 # Number of times message can be received by consumer incase of failure. default is 1.
    sample2:
      prefix: "sample-standard1"
      dlqArn: "arn:aws:sqs:us-east-1:${AWS_ACCOUNT_ID}:sample-standard_DLQ" 
      queueUrl: "https://sqs.us-east-1.amazonaws.com/${AWS_ACCOUNT_ID}/sample-standard"
      sendMessage: true
      respondMessage: false
```
