CREATE VIEW hourly_analytics AS
SELECT (metric || ',' || COALESCE(count, 0)) as kpi
FROM (SELECT 'unique_users'                                                         as "metric",
             (SELECT mv.count FROM unique_users_hourly mv ORDER BY tf DESC LIMIT 1) as count
      UNION ALL
      (SELECT kind, coalesce(mes.count, 0)
       FROM unnest(array ['clicks', 'impressions']) as kind
                LEFT OUTER JOIN (
           (SELECT ueh.count, ueh.counter
            FROM user_events_hourly ueh
            WHERE counter = 'impressions'
               OR counter = 'clicks'
            ORDER BY tf DESC
            LIMIT 2)) mes
                                ON mes.counter = kind)) as pom;
