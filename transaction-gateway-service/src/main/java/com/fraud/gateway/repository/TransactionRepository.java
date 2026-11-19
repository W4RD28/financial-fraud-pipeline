package com.fraud.gateway.repository;

import com.fraud.gateway.domain.TransactionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
  List<TransactionEntity> findByAccountIdOrderByOccurredAtDesc(String accountId);
}
