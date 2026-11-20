package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertReviewEntity;
import java.time.Instant;

public record ReviewResponse(
    Long reviewId,
    String reviewerId,
    AlertStatus oldStatus,
    AlertStatus newStatus,
    String notes,
    Instant reviewedAt) {

  public static ReviewResponse from(FraudAlertReviewEntity review) {
    return new ReviewResponse(
        review.getReviewId(),
        review.getReviewerId(),
        review.getOldStatus(),
        review.getNewStatus(),
        review.getNotes(),
        review.getReviewedAt());
  }
}
