package vinay.messagingsdk.dto;

public record MessageBody(String operationName, String message, String correlationId, String idempotencyKey) {
}
