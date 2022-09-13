CREATE MATERIALIZED VIEW user_events_hourly WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', "timestamp") as tf,
       e.kind || 's'                      as counter,
       COUNT(e.event_id)
FROM events e
WHERE e.timestamp > now() - INTERVAL '1 hour'
GROUP BY tf, kind
;
