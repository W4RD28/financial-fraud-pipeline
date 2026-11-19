package com.fraud.audit.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertEntity {

  @Id
  @Column(name = "alert_id", length = 64)
  private String alertId;

  @Column(name = "transaction_id", nullable = false, unique = true, length = 64)
  private String transactionId;

  @Column(name = "account_id", nullable = false, length = 64)
  private String accountId;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(name = "rule_triggered", nullable = false, length = 64)
  private String ruleTriggered;

  @Column(name = "velocity_count", nullable = false)
  private int velocityCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private AlertStatus status = AlertStatus.OPEN;

  @Column(name = "detected_at", nullable = false)
  private Instant detectedAt;

  @Column(name = "audited_at", nullable = false, updatable = false)
  private Instant auditedAt;

  @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<FraudAlertReviewEntity> reviews = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    if (this.auditedAt == null) {
      this.auditedAt = Instant.now();
    }
  }
}
