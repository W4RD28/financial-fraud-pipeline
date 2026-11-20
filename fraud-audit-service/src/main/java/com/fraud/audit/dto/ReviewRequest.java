package com.fraud.audit.dto;

import com.fraud.audit.domain.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull(message = "Status is required") AlertStatus status, String reviewerId, String notes) {}
