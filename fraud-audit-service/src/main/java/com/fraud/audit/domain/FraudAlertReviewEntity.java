package com.fraud.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "fraud_alert_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertReviewEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id")
  private Long reviewId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "alert_id", nullable = false)
  private FraudAlertEntity alert;

  @Column(name = "reviewer_id", nullable = false, length = 64)
  private String reviewerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "old_status", nullable = false, length = 32)
  private AlertStatus oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false, length = 32)
  private AlertStatus newStatus;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "reviewed_at", nullable = false, updatable = false)
  private Instant reviewedAt;

  @PrePersist
  protected void onCreate() {
    if (this.reviewedAt == null) {
      this.reviewedAt = Instant.now();
    }
  }
}
