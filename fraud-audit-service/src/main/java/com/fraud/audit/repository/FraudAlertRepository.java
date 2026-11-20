package com.fraud.audit.repository;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, String> {

  @EntityGraph(attributePaths = "reviews")
  Page<FraudAlertEntity> findByAccountIdAndStatus(
      String accountId, AlertStatus status, Pageable pageable);

  @EntityGraph(attributePaths = "reviews")
  Page<FraudAlertEntity> findByAccountId(String accountId, Pageable pageable);

  @EntityGraph(attributePaths = "reviews")
  Page<FraudAlertEntity> findByStatus(AlertStatus status, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = "reviews")
  Page<FraudAlertEntity> findAll(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = "reviews")
  Optional<FraudAlertEntity> findById(String alertId);

  List<FraudAlertEntity> findByAccountIdOrderByDetectedAtDesc(String accountId);

  List<FraudAlertEntity> findByStatusOrderByDetectedAtDesc(AlertStatus status);

  List<FraudAlertEntity> findAllByOrderByDetectedAtDesc();

  Optional<FraudAlertEntity> findByTransactionId(String transactionId);

  boolean existsByAlertId(String alertId);

  boolean existsByTransactionId(String transactionId);
}
