package com.fraud.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FraudAlertEvent(
        String alertId,
        String transactionId,
        String accountId,
        BigDecimal amount,
        String ruleTriggered,
        int velocityCount,
        Instant timestamp) {}
