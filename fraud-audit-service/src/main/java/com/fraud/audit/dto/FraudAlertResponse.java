package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FraudAlertResponse(
    String alertId,
    String transactionId,
    String accountId,
    BigDecimal amount,
    String ruleTriggered,
    int velocityCount,
    AlertStatus status,
    Instant detectedAt,
    Instant auditedAt,
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
