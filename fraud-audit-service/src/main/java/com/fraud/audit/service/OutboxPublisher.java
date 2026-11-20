package com.fraud.audit.service;

import com.fraud.audit.domain.AuditOutboxEntity;
import com.fraud.audit.repository.AuditOutboxRepository;
import com.fraud.common.dto.event.TransactionDecisionEvent;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private static final int BATCH_SIZE = 50;
  private static final int MAX_RETRIES = 5;

  private final AuditOutboxRepository outboxRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:500}")
  @Transactional
  public void publishPendingDecisions() {
    List<AuditOutboxEntity> batch = outboxRepository.claimPendingBatch(BATCH_SIZE);
    if (batch.isEmpty()) {
      return;
    }

    for (AuditOutboxEntity record : batch) {
      try {
        TransactionDecisionEvent event =
            objectMapper.readValue(record.getPayload(), TransactionDecisionEvent.class);

        kafkaTemplate
            .send(record.getTopic(), record.getAggregateId(), event)
            .get(5, TimeUnit.SECONDS);

        record.setStatus("PUBLISHED");
        record.setPublishedAt(Instant.now());
        record.setLastError(null);
      } catch (Exception ex) {
        int nextRetry = record.getRetryCount() + 1;
        record.setRetryCount(nextRetry);
        record.setLastError(ex.getMessage());
        if (nextRetry >= MAX_RETRIES) {
          record.setStatus("FAILED");
          log.error(
              "Audit outbox record {} permanently failed after {} attempts for transaction {}",
              record.getOutboxId(),
              nextRetry,
              record.getAggregateId(),
              ex);
        } else {
          log.warn(
              "Audit outbox publish failed (attempt {}/{}) for transaction {}: {}",
              nextRetry,
              MAX_RETRIES,
              record.getAggregateId(),
              ex.getMessage());
        }
      }
    }

    outboxRepository.saveAll(batch);
  }
}
