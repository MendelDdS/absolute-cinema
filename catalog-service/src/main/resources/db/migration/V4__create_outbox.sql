CREATE TABLE outbox (
    id              UUID            PRIMARY KEY,
    aggregate_id    UUID            NOT NULL,
    topic           VARCHAR(200)    NOT NULL,
    event_type      VARCHAR(200)    NOT NULL,
    payload         JSONB           NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox (created_at) WHERE published_at IS NULL;
