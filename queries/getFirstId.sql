WITH all_ids AS (SELECT id
                 FROM SingleOrder
                 UNION ALL
                 SELECT id
                 FROM RepeatedOrder),
     numbers AS (select generate_series from generate_series(1, (SELECT COALESCE(MAX(id), 0) + 1 FROM all_ids)))
select min(generate_series)
from numbers
where generate_series not in (select id from all_ids)