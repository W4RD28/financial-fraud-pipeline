CREATE TABLE fraud_alerts (
    alert_id       VARCHAR(64) PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    account_id     VARCHAR(64) NOT NULL,
    amount         NUMERIC(19, 4) NOT NULL,
    rule_triggered VARCHAR(64) NOT NULL,
    velocity_count INT NOT NULL,
    status         VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    detected_at    TIMESTAMPTZ NOT NULL,
    audited_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fraud_alerts_account_detected 
    ON fraud_alerts (account_id, detected_at DESC);

CREATE INDEX idx_fraud_alerts_status 
    ON fraud_alerts (status);

CREATE TABLE fraud_alert_reviews (
    review_id   BIGSERIAL PRIMARY KEY,
    alert_id    VARCHAR(64) NOT NULL REFERENCES fraud_alerts(alert_id) ON DELETE CASCADE,
    reviewer_id VARCHAR(64) NOT NULL,
    old_status  VARCHAR(32) NOT NULL,
    new_status  VARCHAR(32) NOT NULL,
    notes       TEXT,
    reviewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alert_reviews_alert_id 
    ON fraud_alert_reviews (alert_id);
