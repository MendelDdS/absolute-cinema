CREATE TABLE sessions (
    id          UUID            PRIMARY KEY,
    starts_at   TIMESTAMPTZ     NOT NULL,
    price       NUMERIC(10, 2)  NOT NULL CHECK (price >= 0)
);

CREATE TABLE session_seats (
    session_id      UUID                    NOT NULL REFERENCES sessions (id) ON DELETE CASCADE,
    seat_order      INTEGER                 NOT NULL CHECK (seat_order >= 0),
    seat_code       VARCHAR(10)             NOT NULL CHECK (seat_code ~ '^[A-Z][1-9][0-9]*$'),
    PRIMARY KEY     (session_id, seat_code),
    UNIQUE          (session_id, seat_order)
);
