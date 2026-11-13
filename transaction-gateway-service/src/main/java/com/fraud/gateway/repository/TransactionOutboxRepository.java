package com.fraud.gateway.repository;

import com.fraud.gateway.domain.OutboxStatus;
import com.fraud.gateway.domain.TransactionOutboxEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionOutboxRepository extends JpaRepository<TransactionOutboxEntity, UUID> {
    List<TransactionOutboxEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
