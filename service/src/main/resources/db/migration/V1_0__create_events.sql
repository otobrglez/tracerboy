CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE events
(
    event_id  UUID         NOT NULL,
    kind      varchar(255) NOT NULL,
    timestamp TIMESTAMPTZ  NOT NULL,
    username  varchar(255) NOT NULL
    -- PRIMARY KEY (timestamp, username)
);

SELECT create_hypertable('events', 'timestamp');

CREATE INDEX IF NOT EXISTS ix_kind_time ON events (kind, timestamp DESC);

CREATE MATERIALIZED VIEW unique_users_hourly WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', "timestamp") as tf,
       COUNT(DISTINCT e.username)
FROM events e
WHERE e.timestamp > now() - INTERVAL '1 hour'
GROUP BY tf
;

SELECT add_retention_policy('unique_users_hourly', INTERVAL '7 days');
