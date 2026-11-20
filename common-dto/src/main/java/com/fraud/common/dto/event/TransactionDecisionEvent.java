package com.fraud.common.dto.event;

import java.time.Instant;

public record TransactionDecisionEvent(
    String transactionId,
    String accountId,
    TransactionDecisionStatus decision,
    Double riskScore,
    String triggeredRule,
    String reason,
    String decidedBy,
    Instant decidedAt) {}
