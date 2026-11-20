package com.fraud.gateway.domain;

import com.fraud.common.dto.event.TransactionDecisionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

  @Id
  @Column(name = "transaction_id", length = 64)
  private String transactionId;

  @Column(name = "account_id", nullable = false, length = 64)
  private String accountId;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "merchant_category_code", nullable = false, length = 16)
  private String merchantCategoryCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private TransactionDecisionStatus status = TransactionDecisionStatus.PENDING_REVIEW;

  @Column(name = "risk_score")
  private Double riskScore;

  @Column(name = "decision_reason", length = 512)
  private String decisionReason;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "decided_by", length = 128)
  private String decidedBy;

  @Column(name = "decided_at")
  private Instant decidedAt;

  @PrePersist
  protected void onCreate() {
    if (this.status == null) {
      this.status = TransactionDecisionStatus.PENDING_REVIEW;
    }
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
  }
}
