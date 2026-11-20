package com.fraud.audit.service;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import com.fraud.audit.domain.FraudAlertReviewEntity;
import com.fraud.audit.dto.FraudAlertResponse;
import com.fraud.audit.dto.ReviewRequest;
import com.fraud.audit.repository.FraudAlertRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

  private final FraudAlertRepository alertRepository;

  @Transactional(readOnly = true)
  public Page<FraudAlertResponse> listAlerts(
      String accountId, AlertStatus status, Pageable pageable) {
    boolean hasAccount = accountId != null && !accountId.isBlank();
    boolean hasStatus = status != null;

    Page<FraudAlertEntity> page;
    if (hasAccount && hasStatus) {
      page = alertRepository.findByAccountIdAndStatus(accountId, status, pageable);
    } else if (hasAccount) {
      page = alertRepository.findByAccountId(accountId, pageable);
    } else if (hasStatus) {
      page = alertRepository.findByStatus(status, pageable);
    } else {
      page = alertRepository.findAll(pageable);
    }
    return page.map(FraudAlertResponse::from);
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
