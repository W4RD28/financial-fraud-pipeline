package com.fraud.gateway.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "transaction_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionOutboxEntity {

  @Id
  @Column(name = "outbox_id")
  private UUID outboxId;

  @Column(name = "transaction_id", nullable = false, length = 64)
  private String transactionId;

  @Column(name = "topic", nullable = false, length = 128)
  private String topic;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private OutboxStatus status = OutboxStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @PrePersist
  protected void onCreate() {
    if (this.outboxId == null) {
      this.outboxId = UUID.randomUUID();
    }
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
  }
}
