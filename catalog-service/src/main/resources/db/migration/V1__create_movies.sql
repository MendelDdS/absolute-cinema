CREATE TABLE movies (
    id                  UUID            PRIMARY KEY,
    title               VARCHAR(200)    NOT NULL,
    duration_minutes    INTEGER         NOT NULL CHECK (duration_minutes > 0)
);