package com.fraud.audit.repository;

import com.fraud.audit.domain.AuditOutboxEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditOutboxRepository extends JpaRepository<AuditOutboxEntity, UUID> {

  @Query(
      value =
          """
      SELECT * FROM audit.audit_outbox
      WHERE status = 'PENDING'
      ORDER BY created_at ASC
      LIMIT :batchSize
      FOR UPDATE SKIP LOCKED
      """,
      nativeQuery = true)
  List<AuditOutboxEntity> claimPendingBatch(@Param("batchSize") int batchSize);
}
