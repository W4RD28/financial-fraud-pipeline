package com.fraud.audit.consumer;

import com.fraud.audit.domain.FraudAlertEntity;
import com.fraud.audit.repository.FraudAlertRepository;
import com.fraud.common.dto.FraudAlertEvent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAlertConsumer {

  private final FraudAlertRepository alertRepository;

  @KafkaListener(
      topics = "${app.kafka.topics.fraud-alerts:fraud-alerts}",
      groupId = "${spring.kafka.consumer.group-id:fraud-audit-group}")
  @Transactional
  public void onFraudAlert(FraudAlertEvent event) {
    if (alertRepository.existsByAlertId(event.alertId())
        || alertRepository.existsByTransactionId(event.transactionId())) {
      log.info(
          "Skipping duplicate fraud alert alertId={} transactionId={}",
          event.alertId(),
          event.transactionId());
      return;
    }

    FraudAlertEntity entity =
        FraudAlertEntity.builder()
            .alertId(event.alertId())
            .transactionId(event.transactionId())
            .accountId(event.accountId())
            .amount(event.amount())
            .ruleTriggered(event.ruleTriggered())
            .velocityCount(event.velocityCount())
            .detectedAt(event.timestamp() != null ? event.timestamp() : Instant.now())
            .build();

    alertRepository.save(entity);
    log.info(
        "Persisted fraud alert alertId={} accountId={} rule={}",
        event.alertId(),
        event.accountId(),
        event.ruleTriggered());
  }
}
