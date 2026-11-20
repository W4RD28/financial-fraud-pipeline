package com.fraud.audit.service;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import com.fraud.audit.domain.FraudAlertReviewEntity;
import com.fraud.audit.dto.FraudAlertResponse;
import com.fraud.audit.dto.ReviewRequest;
import com.fraud.audit.repository.FraudAlertRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

  private final FraudAlertRepository alertRepository;

  @Transactional(readOnly = true)
  public List<FraudAlertResponse> listAlerts(String accountId, AlertStatus status) {
    List<FraudAlertEntity> alerts;
    if (accountId != null && !accountId.isBlank()) {
      alerts = alertRepository.findByAccountIdOrderByDetectedAtDesc(accountId);
    } else if (status != null) {
      alerts = alertRepository.findByStatusOrderByDetectedAtDesc(status);
    } else {
      alerts = alertRepository.findAllByOrderByDetectedAtDesc();
    }
    return alerts.stream().map(FraudAlertResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public Optional<FraudAlertResponse> getAlert(String alertId) {
    return alertRepository.findById(alertId).map(FraudAlertResponse::from);
  }

  @Transactional
  public Optional<FraudAlertResponse> reviewAlert(String alertId, ReviewRequest request) {
    return alertRepository
        .findById(alertId)
        .map(
            alert -> {
              AlertStatus previousStatus = alert.getStatus();
              alert.setStatus(request.status());

              FraudAlertReviewEntity review =
                  FraudAlertReviewEntity.builder()
                      .alert(alert)
                      .reviewerId(
                          request.reviewerId() != null && !request.reviewerId().isBlank()
                              ? request.reviewerId()
                              : "system-analyst")
                      .oldStatus(previousStatus)
                      .newStatus(request.status())
                      .notes(request.notes())
                      .build();

              alert.getReviews().add(review);
              FraudAlertEntity saved = alertRepository.save(alert);
              return FraudAlertResponse.from(saved);
            });
  }
}
