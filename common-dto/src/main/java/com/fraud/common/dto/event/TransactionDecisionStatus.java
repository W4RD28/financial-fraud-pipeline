package com.fraud.common.dto.event;

public enum TransactionDecisionStatus {
  PENDING_REVIEW,
  APPROVED,
  HELD_FOR_REVIEW,
  DECLINED,
  OVERRIDDEN_APPROVED,
  OVERRIDDEN_DECLINED
}
