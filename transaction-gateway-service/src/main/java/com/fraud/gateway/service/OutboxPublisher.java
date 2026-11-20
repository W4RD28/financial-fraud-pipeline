package com.fraud.gateway.service;

import com.fraud.common.dto.event.TransactionEvent;
import com.fraud.gateway.domain.OutboxStatus;
import com.fraud.gateway.domain.TransactionOutboxEntity;
import com.fraud.gateway.repository.TransactionOutboxRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private static final int BATCH_SIZE = 50;

  private final TransactionOutboxRepository outboxRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
  private String rawTransactionsTopic;

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:500}")
  @Transactional
  public void publishPendingEvents() {
    List<TransactionOutboxEntity> pendingBatch =
        outboxRepository.findPendingBatchForUpdate(BATCH_SIZE);

    if (pendingBatch.isEmpty()) {
      return;
    }

    for (TransactionOutboxEntity entry : pendingBatch) {
      try {
        TransactionEvent event = objectMapper.readValue(entry.getPayload(), TransactionEvent.class);

        // Use accountId as key so all transactions for an account land on the same
        // partition
        kafkaTemplate.send(rawTransactionsTopic, event.accountId(), event).get(5, TimeUnit.SECONDS);

        entry.setStatus(OutboxStatus.PUBLISHED);
        entry.setPublishedAt(Instant.now());
      } catch (Exception ex) {
        log.error("Failed to publish outbox event {} (aggregateId={}): {}", entry, ex.getMessage());
      }
    }

    outboxRepository.saveAll(pendingBatch);
  }
}
