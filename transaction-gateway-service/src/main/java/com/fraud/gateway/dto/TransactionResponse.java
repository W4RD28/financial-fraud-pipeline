package com.fraud.gateway.dto;

import com.fraud.gateway.domain.TransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String transactionId,
        String accountId,
        BigDecimal amount,
        String currency,
        String merchantCategoryCode,
        Instant occurredAt,
        Instant createdAt) {
    public static TransactionResponse fromEntity(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getMerchantCategoryCode(),
                entity.getOccurredAt(),
                entity.getCreatedAt());
    }
}
