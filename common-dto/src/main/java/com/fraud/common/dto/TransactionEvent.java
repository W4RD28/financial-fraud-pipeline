package com.fraud.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
    String transactionId,
    String accountId,
    BigDecimal amount,
    String currency,
    String merchantCategoryCode,
    Instant timestamp) {}
