package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertReviewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Immutable audit entry representing a single analyst review action")
public record ReviewResponse(
    @Schema(description = "Auto-generated review sequence ID", example = "1") Long reviewId,
    @Schema(
            description = "Identifier of the analyst who performed the review",
            example = "windu-analyst")
        String reviewerId,
    @Schema(description = "Alert status prior to this review", example = "OPEN")
        AlertStatus oldStatus,
    @Schema(description = "Alert status after this review", example = "CONFIRMED_FRAUD")
        AlertStatus newStatus,
    @Schema(
            description = "Analyst justification notes",
            example = "Single high-value luxury purchase exceeding $5,000 threshold.")
        String notes,
    @Schema(description = "UTC timestamp when the review was recorded") Instant reviewedAt) {

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
