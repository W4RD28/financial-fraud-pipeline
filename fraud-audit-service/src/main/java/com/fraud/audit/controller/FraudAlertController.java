package com.fraud.audit.controller;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.dto.FraudAlertResponse;
import com.fraud.audit.dto.ReviewRequest;
import com.fraud.audit.service.FraudAlertService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fraud-alerts")
@RequiredArgsConstructor
public class FraudAlertController {

  private final FraudAlertService fraudAlertService;

  @GetMapping
  public ResponseEntity<List<FraudAlertResponse>> listAlerts(
      @RequestParam(required = false) String accountId,
      @RequestParam(required = false) AlertStatus status) {
    return ResponseEntity.ok(fraudAlertService.listAlerts(accountId, status));
  }

  @GetMapping("/{alertId}")
  public ResponseEntity<FraudAlertResponse> getAlert(@PathVariable String alertId) {
    return fraudAlertService
        .getAlert(alertId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PatchMapping("/{alertId}/review")
  public ResponseEntity<FraudAlertResponse> reviewAlert(
      @PathVariable String alertId, @Valid @RequestBody ReviewRequest request) {
    return fraudAlertService
        .reviewAlert(alertId, request)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
