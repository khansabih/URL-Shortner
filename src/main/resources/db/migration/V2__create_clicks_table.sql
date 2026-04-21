CREATE TABLE clicks(
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES urls(id),
    short_code VARCHAR(10) NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_hash VARCHAR(64),
    user_agent TEXT,
    device_type VARCHAR(20),
    referrer TEXT
);

CREATE INDEX idx_clicks_short_code ON clicks(short_code);
CREATE INDEX idx_clicks_url_id ON clicks(url_id);
CREATE INDEX idx_clicks_clicked_at ON clicks(clicked_at);