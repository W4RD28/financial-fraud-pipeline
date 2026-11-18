package com.fraud.edge.repository;

import com.fraud.edge.domain.RequestLogEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogRepository extends ReactiveCrudRepository<RequestLogEntity, String> {}
