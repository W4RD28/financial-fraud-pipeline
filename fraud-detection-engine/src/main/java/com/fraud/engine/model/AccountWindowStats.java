package com.fraud.engine.model;

import com.fraud.common.dto.TransactionEvent;
import java.math.BigDecimal;

public record AccountWindowStats(
    String accountId, long transactionCount, BigDecimal totalAmount, String latestTransactionId) {

  public static AccountWindowStats empty() {
    return new AccountWindowStats("", 0L, BigDecimal.ZERO, null);
  }

  public AccountWindowStats add(TransactionEvent event) {
    BigDecimal txAmount = event.amount() != null ? event.amount() : BigDecimal.ZERO;
    return new AccountWindowStats(
        event.accountId(),
        this.transactionCount + 1,
        this.totalAmount.add(txAmount),
        event.transactionId().toString());
  }
}
