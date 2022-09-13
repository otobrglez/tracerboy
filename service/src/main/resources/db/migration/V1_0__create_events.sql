CREATE TABLE IF NOT EXISTS events
(
    event_id  UUID         NOT NULL,
    kind      varchar(255) NOT NULL,
    timestamp TIMESTAMPTZ  NOT NULL,
    username  varchar(255) NOT NULL
    -- PRIMARY KEY (timestamp, username)
);
