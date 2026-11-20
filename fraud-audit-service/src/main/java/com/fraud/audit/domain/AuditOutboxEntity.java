package com.fraud.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "audit_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditOutboxEntity {

  @Id
  @Column(name = "outbox_id")
  private UUID outboxId;

  @Column(name = "aggregate_id", nullable = false, length = 64)
  private String aggregateId;

  @Column(name = "topic", nullable = false, length = 128)
  private String topic;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "status", nullable = false, length = 32)
  @Builder.Default
  private String status = "PENDING";

  @Column(name = "retry_count", nullable = false)
  @Builder.Default
  private int retryCount = 0;

  @Column(name = "last_error", columnDefinition = "TEXT")
  private String lastError;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @PrePersist
  protected void onCreate() {
    if (this.outboxId == null) {
      this.outboxId = UUID.randomUUID();
    }
    if (this.status == null) {
      this.status = "PENDING";
    }
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
  }
}
