ALTER TABLE gateway.transactions
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    ADD COLUMN IF NOT EXISTS risk_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS decision_reason VARCHAR(512),
    ADD COLUMN IF NOT EXISTS decided_by VARCHAR(128),
    ADD COLUMN IF NOT EXISTS decided_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_transactions_status_occurred_at
    ON gateway.transactions (status, occurred_at DESC);
