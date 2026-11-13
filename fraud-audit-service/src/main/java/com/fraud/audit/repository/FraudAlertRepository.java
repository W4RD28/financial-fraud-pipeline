package com.fraud.audit.repository;

import com.fraud.audit.domain.AlertStatus;
import com.fraud.audit.domain.FraudAlertEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, String> {
    List<FraudAlertEntity> findByAccountIdOrderByDetectedAtDesc(String accountId);

    List<FraudAlertEntity> findByStatusOrderByDetectedAtDesc(AlertStatus status);

    Optional<FraudAlertEntity> findByTransactionId(String transactionId);

    boolean existsByAlertId(String alertId);
}
