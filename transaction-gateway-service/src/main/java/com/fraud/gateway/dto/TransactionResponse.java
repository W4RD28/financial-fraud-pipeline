package com.fraud.gateway.dto;

import com.fraud.common.dto.event.TransactionDecisionStatus;
import com.fraud.gateway.domain.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    String transactionId,
    String accountId,
    BigDecimal amount,
    String currency,
    String merchantCategoryCode,
    TransactionDecisionStatus status,
    Double riskScore,
    String decisionReason,
    String decidedBy,
    Instant occurredAt,
    Instant createdAt,
    Instant decidedAt) {

  public static TransactionResponse fromEntity(TransactionEntity entity) {
    return new TransactionResponse(
        entity.getTransactionId(),
        entity.getAccountId(),
        entity.getAmount(),
        entity.getCurrency(),
        entity.getMerchantCategoryCode(),
        entity.getStatus(),
        entity.getRiskScore(),
        entity.getDecisionReason(),
        entity.getDecidedBy(),
        entity.getOccurredAt(),
        entity.getCreatedAt(),
        entity.getDecidedAt());
  }
}
