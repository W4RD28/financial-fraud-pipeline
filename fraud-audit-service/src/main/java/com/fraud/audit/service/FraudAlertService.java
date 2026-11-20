package com.fraud.audit.service;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.AuditOutboxEntity;
import com.fraud.audit.domain.FraudAlertEntity;
import com.fraud.audit.domain.FraudAlertReviewEntity;
import com.fraud.audit.dto.FraudAlertResponse;
import com.fraud.audit.dto.ReviewRequest;
import com.fraud.audit.repository.AuditOutboxRepository;
import com.fraud.audit.repository.FraudAlertRepository;
import com.fraud.common.dto.event.TransactionDecisionEvent;
import com.fraud.common.dto.event.TransactionDecisionStatus;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

  private final FraudAlertRepository alertRepository;
  private final AuditOutboxRepository auditOutboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.transaction-decisions:transaction-decisions}")
  private String transactionDecisionsTopic;

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
              String effectiveReviewer =
                  request.reviewerId() != null && !request.reviewerId().isBlank()
                      ? request.reviewerId()
                      : "system-analyst";

              alert.setStatus(request.status());

              FraudAlertReviewEntity review =
                  FraudAlertReviewEntity.builder()
                      .alert(alert)
                      .reviewerId(effectiveReviewer)
                      .oldStatus(previousStatus)
                      .newStatus(request.status())
                      .notes(request.notes())
                      .build();

              alert.getReviews().add(review);
              FraudAlertEntity saved = alertRepository.save(alert);

              resolveOverrideDecision(request.status())
                  .ifPresent(
                      overrideStatus ->
                          stageDecisionOutboxEvent(
                              saved, overrideStatus, effectiveReviewer, request.notes()));

              return FraudAlertResponse.from(saved);
            });
  }

  private Optional<TransactionDecisionStatus> resolveOverrideDecision(AlertStatus status) {
    return switch (status) {
      case FALSE_POSITIVE -> Optional.of(TransactionDecisionStatus.OVERRIDDEN_APPROVED);
      case CONFIRMED_FRAUD -> Optional.of(TransactionDecisionStatus.OVERRIDDEN_DECLINED);
      default -> Optional.empty();
    };
  }

  private void stageDecisionOutboxEvent(
      FraudAlertEntity alert,
      TransactionDecisionStatus overrideStatus,
      String reviewerId,
      String notes) {
    TransactionDecisionEvent decisionEvent =
        new TransactionDecisionEvent(
            alert.getTransactionId(),
            alert.getAccountId(),
            overrideStatus,
            null,
            alert.getRuleTriggered(),
            notes,
            "ANALYST:" + reviewerId,
            Instant.now());

    AuditOutboxEntity outboxRecord =
        AuditOutboxEntity.builder()
            .aggregateId(alert.getTransactionId())
            .topic(transactionDecisionsTopic)
            .payload(serializeEvent(decisionEvent))
            .status("PENDING")
            .build();

    auditOutboxRepository.save(outboxRecord);
  }

  private String serializeEvent(TransactionDecisionEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JacksonException e) {
      throw new IllegalStateException(
          "Failed to serialize TransactionDecisionEvent for transaction " + event.transactionId(),
          e);
    }
  }
}
