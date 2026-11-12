CREATE TABLE rooms (
    id      UUID            PRIMARY KEY,
    name    VARCHAR(100)    NOT NULL
);

CREATE TABLE room_seats (
    room_id     UUID        NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    seat_row    VARCHAR(1)  NOT NULL CHECK (seat_row ~ '^[A-Z]$'),
    seat_number INTEGER     NOT NULL CHECK (seat_number > 0),
    PRIMARY KEY (room_id, seat_row, seat_number)
);