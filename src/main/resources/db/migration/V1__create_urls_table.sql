CREATE TABLE urls(
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL
);

CREATE INDEX idx_urls_short_code ON urls(short_code);

ALTER SEQUENCE urls_id_seq RESTART WITH 1000000;
