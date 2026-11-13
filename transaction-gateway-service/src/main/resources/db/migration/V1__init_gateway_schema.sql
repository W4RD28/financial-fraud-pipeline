CREATE TABLE transactions (
    transaction_id         VARCHAR(64) PRIMARY KEY,
    account_id             VARCHAR(64) NOT NULL,
    amount                 NUMERIC(19, 4) NOT NULL,
    currency               CHAR(3) NOT NULL,
    merchant_category_code VARCHAR(16) NOT NULL,
    occurred_at            TIMESTAMPTZ NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_occurred 
    ON transactions (account_id, occurred_at DESC);

CREATE TABLE transaction_outbox (
    outbox_id      UUID PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL REFERENCES transactions(transaction_id),
    topic          VARCHAR(128) NOT NULL,
    payload        JSONB NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status_created 
    ON transaction_outbox (status, created_at) 
    WHERE status = 'PENDING';
