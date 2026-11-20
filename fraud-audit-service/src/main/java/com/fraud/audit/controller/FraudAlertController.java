package com.fraud.audit.controller;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.dto.FraudAlertResponse;
import com.fraud.audit.dto.ReviewRequest;
import com.fraud.audit.service.FraudAlertService;
import com.fraud.common.dto.api.ApiErrorDetail;
import com.fraud.common.dto.api.ApiResponse;
import com.fraud.common.dto.api.PageMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/fraud-alerts")
@RequiredArgsConstructor
@Tag(
    name = "Fraud Alerts & Triage",
    description = "Query detected fraud alerts and record analyst investigation decisions")
public class FraudAlertController {

  private final FraudAlertService alertService;

  @GetMapping
  @Operation(
      summary = "List fraud alerts with filters and pagination",
      description =
          "Returns a paginated list of persisted fraud alerts, optionally filtered by accountId and/or triage status.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Paginated fraud alerts retrieved successfully"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Invalid filter or pagination parameter",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<List<FraudAlertResponse>>> getAlerts(
      @Parameter(description = "Filter by customer account ID", example = "ACC-1001")
          @RequestParam(required = false)
          String accountId,
      @Parameter(description = "Filter by alert review status", example = "OPEN")
          @RequestParam(required = false)
          AlertStatus status,
      @ParameterObject
          @PageableDefault(size = 20, sort = "detectedAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    Page<FraudAlertResponse> page = alertService.listAlerts(accountId, status, pageable);
    PageMeta meta =
        PageMeta.ofZeroIndexed(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious());
    return ResponseEntity.ok(ApiResponse.paged(page.getContent(), meta));
  }

  @GetMapping("/{alertId}")
  @Operation(
      summary = "Get a single fraud alert by ID",
      description = "Fetches detailed alert metadata, triage state, and review history.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Fraud alert found"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Fraud alert does not exist",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<FraudAlertResponse>> getAlertById(
      @Parameter(
              description = "Unique ID of the fraud alert",
              example = "4f5a3b86-f100-46ef-ae0c-242cdfa575d3")
          @PathVariable
          String alertId) {
    FraudAlertResponse alert =
        alertService
            .getAlert(alertId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fraud alert not found: " + alertId));
    return ResponseEntity.ok(ApiResponse.ok(alert));
  }

  @PatchMapping("/{alertId}/review")
  @Operation(
      summary = "Record an analyst review decision",
      description =
          "Updates the triage status of a fraud alert and writes an immutable audit trail record.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Alert status updated and review persisted"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Validation error in review payload",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "Fraud alert does not exist",
        content = @Content(schema = @Schema(implementation = ApiErrorDetail.class)))
  })
  public ResponseEntity<ApiResponse<FraudAlertResponse>> reviewAlert(
      @Parameter(description = "Unique ID of the fraud alert to review") @PathVariable
          String alertId,
      @Valid @RequestBody ReviewRequest request) {
    FraudAlertResponse updated =
        alertService
            .reviewAlert(alertId, request)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fraud alert not found: " + alertId));
    return ResponseEntity.ok(ApiResponse.ok("Alert review recorded", updated));
  }
}
