CREATE TABLE request_logs (
    request_id     VARCHAR(64) PRIMARY KEY,
    method         VARCHAR(10) NOT NULL,
    path           VARCHAR(255) NOT NULL,
    query_params   TEXT,
    client_ip      VARCHAR(64),
    user_agent     TEXT,
    target_service VARCHAR(64),
    status_code    INT,
    duration_ms    BIGINT NOT NULL,
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_request_logs_requested_at ON request_logs (requested_at DESC);
CREATE INDEX idx_request_logs_path_status ON request_logs (path, status_code);
