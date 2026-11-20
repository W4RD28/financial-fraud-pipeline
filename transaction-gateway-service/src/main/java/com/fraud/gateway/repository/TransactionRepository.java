package com.fraud.gateway.repository;

import com.fraud.gateway.domain.TransactionEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

  Page<TransactionEntity> findByAccountId(String accountId, Pageable pageable);

  List<TransactionEntity> findByAccountIdOrderByOccurredAtDesc(String accountId);

  @Modifying
  @Query(
      value =
          """
      UPDATE gateway.transactions
      SET status = :status,
          risk_score = :riskScore,
          decision_reason = :reason,
          decided_by = :decidedBy,
          decided_at = :decidedAt
      WHERE transaction_id = :transactionId
        AND status NOT IN ('OVERRIDDEN_APPROVED', 'OVERRIDDEN_DECLINED')
      """,
      nativeQuery = true)
  int applyAutomatedDecision(
      @Param("transactionId") String transactionId,
      @Param("status") String status,
      @Param("riskScore") Double riskScore,
      @Param("reason") String reason,
      @Param("decidedBy") String decidedBy,
      @Param("decidedAt") Instant decidedAt);

  @Modifying
  @Query(
      value =
          """
      UPDATE gateway.transactions
      SET status = :status,
          decision_reason = :reason,
          decided_by = :decidedBy,
          decided_at = :decidedAt
      WHERE transaction_id = :transactionId
      """,
      nativeQuery = true)
  int applyAnalystOverride(
      @Param("transactionId") String transactionId,
      @Param("status") String status,
      @Param("reason") String reason,
      @Param("decidedBy") String decidedBy,
      @Param("decidedAt") Instant decidedAt);
}
