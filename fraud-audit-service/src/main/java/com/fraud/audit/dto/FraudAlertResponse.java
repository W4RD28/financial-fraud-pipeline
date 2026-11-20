package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Persisted fraud alert record with current triage status and review history")
public record FraudAlertResponse(
    @Schema(
            description = "Unique identifier of the fraud alert",
            example = "4f5a3b86-f100-46ef-ae0c-242cdfa575d3")
        String alertId,
    @Schema(
            description = "Triggering transaction identifier",
            example = "ff5acd02-0d98-483b-928e-f0cfd86af69b")
        String transactionId,
    @Schema(description = "Customer account identifier", example = "ACC-2002") String accountId,
    @Schema(
            description = "Total monetary amount aggregated within the detection window",
            example = "6500.00")
        BigDecimal amount,
    @Schema(
            description = "Fraud rule that triggered the alert",
            example = "HIGH_VALUE_WINDOW_EXCEEDED")
        String ruleTriggered,
    @Schema(
            description = "Number of transactions counted inside the tumbling window",
            example = "1")
        int velocityCount,
    @Schema(description = "Current investigation lifecycle status", example = "OPEN")
        AlertStatus status,
    @Schema(description = "UTC timestamp when the alert was emitted by the detection engine")
        Instant detectedAt,
    @Schema(description = "UTC timestamp when the alert was persisted by the audit service")
        Instant auditedAt,
    @Schema(description = "Chronological history of analyst triage reviews recorded for this alert")
        List<ReviewResponse> reviews) {

  public static FraudAlertResponse from(FraudAlertEntity entity) {
    return new FraudAlertResponse(
        entity.getAlertId(),
        entity.getTransactionId(),
        entity.getAccountId(),
        entity.getAmount(),
        entity.getRuleTriggered(),
        entity.getVelocityCount(),
        entity.getStatus(),
        entity.getDetectedAt(),
        entity.getAuditedAt(),
        entity.getReviews().stream().map(ReviewResponse::from).toList());
  }
}
