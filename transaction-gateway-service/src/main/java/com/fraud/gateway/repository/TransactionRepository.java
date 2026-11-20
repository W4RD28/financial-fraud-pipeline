package com.fraud.gateway.repository;

import com.fraud.gateway.domain.TransactionEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

  Page<TransactionEntity> findByAccountId(String accountId, Pageable pageable);

  List<TransactionEntity> findByAccountIdOrderByOccurredAtDesc(String accountId);
}
