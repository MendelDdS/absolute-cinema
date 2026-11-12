CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE sessions (
    id          UUID            PRIMARY KEY,
    movie_id    UUID            NOT NULL REFERENCES movies (id),
    room_id     UUID            NOT NULL REFERENCES rooms  (id),
    starts_at   TIMESTAMPTZ     NOT NULL,
    ends_at     TIMESTAMPTZ     NOT NULL,
    price       NUMERIC(10, 2)  NOT NULL CHECK (price >= 0),
    CHECK (ends_at > starts_at),

    CONSTRAINT  sessions_no_overlap
        EXCLUDE USING gist (room_id WITH =, tstzrange(starts_at, ends_at) WITH &&)
);

CREATE INDEX idx_sessions_starts_at ON sessions (starts_at);
CREATE INDEX idx_sessions_movie_starts_at ON sessions (movie_id, starts_at);
