package com.fraud.gateway.consumer;

import com.fraud.common.dto.event.TransactionDecisionEvent;
import com.fraud.common.dto.event.TransactionDecisionStatus;
import com.fraud.gateway.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionDecisionConsumer {

  private final TransactionRepository transactionRepository;

  @KafkaListener(
      topics = "${app.kafka.topics.transaction-decisions:transaction-decisions}",
      groupId = "${spring.application.name}-decision-consumer")
  @Transactional
  public void onDecision(TransactionDecisionEvent event) {
    boolean isAnalystOverride =
        event.decision() == TransactionDecisionStatus.OVERRIDDEN_APPROVED
            || event.decision() == TransactionDecisionStatus.OVERRIDDEN_DECLINED;

    int updated =
        isAnalystOverride
            ? transactionRepository.applyAnalystOverride(
                event.transactionId(),
                event.decision().name(),
                event.reason(),
                event.decidedBy(),
                event.decidedAt())
            : transactionRepository.applyAutomatedDecision(
                event.transactionId(),
                event.decision().name(),
                event.riskScore(),
                event.reason(),
                event.decidedBy(),
                event.decidedAt());

    if (updated == 0) {
      log.warn(
          "Skipped decision {} for transaction {} (not found or already analyst-overridden)",
          event.decision(),
          event.transactionId());
      return;
    }

    log.info(
        "Transaction {} transitioned to {} (by={}, rule={})",
        event.transactionId(),
        event.decision(),
        event.decidedBy(),
        event.triggeredRule());
  }
}
