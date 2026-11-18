package com.fraud.gateway.service;

import com.fraud.common.dto.TransactionEvent;
import com.fraud.gateway.domain.OutboxStatus;
import com.fraud.gateway.domain.TransactionEntity;
import com.fraud.gateway.domain.TransactionOutboxEntity;
import com.fraud.gateway.dto.CreateTransactionRequest;
import com.fraud.gateway.dto.TransactionResponse;
import com.fraud.gateway.repository.TransactionOutboxRepository;
import com.fraud.gateway.repository.TransactionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.raw-transactions:raw-transactions}")
    private String rawTransactionsTopic;

    @Transactional
    public TransactionResponse ingestTransaction(CreateTransactionRequest request) {
        String txId =
                (request.transactionId() != null && !request.transactionId().isBlank())
                        ? request.transactionId()
                        : UUID.randomUUID().toString();

        if (transactionRepository.existsById(txId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Transaction already exists: " + txId);
        }

        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();

        TransactionEntity entity =
                TransactionEntity.builder()
                        .transactionId(txId)
                        .accountId(request.accountId())
                        .amount(request.amount())
                        .currency(request.currency())
                        .merchantCategoryCode(request.merchantCategoryCode())
                        .occurredAt(occurredAt)
                        .build();

        TransactionEntity saved = transactionRepository.save(entity);

        // Stage event in transaction_outbox for reliable Kafka publishing
        TransactionEvent event =
                new TransactionEvent(
                        saved.getTransactionId(),
                        saved.getAccountId(),
                        saved.getAmount(),
                        saved.getCurrency(),
                        saved.getMerchantCategoryCode(),
                        saved.getOccurredAt());

        TransactionOutboxEntity outbox =
                TransactionOutboxEntity.builder()
                        .transactionId(saved.getTransactionId())
                        .topic(rawTransactionsTopic)
                        .payload(objectMapper.writeValueAsString(event))
                        .status(OutboxStatus.PENDING)
                        .build();

        outboxRepository.save(outbox);

        log.info(
                "Persisted transaction {} for account {} (amount: {} {})",
                saved.getTransactionId(),
                saved.getAccountId(),
                saved.getAmount(),
                saved.getCurrency());

        // TODO: publish 'event' to Kafka topic 'raw-transactions' and mark outbox
        // PUBLISHED

        return TransactionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(String transactionId) {
        return transactionRepository
                .findById(transactionId)
                .map(TransactionResponse::fromEntity)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Transaction not found: " + transactionId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getByAccountId(String accountId) {
        return transactionRepository.findByAccountIdOrderByOccurredAtDesc(accountId).stream()
                .map(TransactionResponse::fromEntity)
                .toList();
    }
}
