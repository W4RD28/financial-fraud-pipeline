package com.fraud.gateway.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotBlank(message = "accountId is required") @Size(max = 64, message = "accountId must not exceed 64 characters") String accountId,
    @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be greater than zero") BigDecimal amount,
    @NotBlank(message = "currency is required") @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "currency must be a 3-letter ISO code (e.g., USD, IDR)")
        String currency,
    @NotBlank(message = "merchantCategoryCode is required") @Size(max = 16, message = "merchantCategoryCode must not exceed 16 characters") String merchantCategoryCode) {}
