package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Analyst triage decision payload for reviewing a fraud alert")
public record ReviewRequest(
    @Schema(description = "New triage status to assign to the alert", example = "CONFIRMED_FRAUD")
        @NotNull(message = "Status is required") AlertStatus status,
    @Schema(
            description = "Optional identifier of the analyst recording the review",
            example = "windu-analyst",
            nullable = true)
        String reviewerId,
    @Schema(
            description = "Optional audit justification notes explaining the triage decision",
            example = "Single high-value luxury purchase exceeding $5,000 threshold.",
            nullable = true)
        String notes) {}
