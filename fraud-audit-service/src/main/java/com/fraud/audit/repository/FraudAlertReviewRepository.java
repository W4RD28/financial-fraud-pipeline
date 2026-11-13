package com.fraud.audit.repository;

import com.fraud.audit.domain.FraudAlertReviewEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudAlertReviewRepository extends JpaRepository<FraudAlertReviewEntity, Long> {
    List<FraudAlertReviewEntity> findByAlert_AlertIdOrderByReviewedAtAsc(String alertId);
}
