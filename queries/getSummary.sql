SELECT orderid, descr, MIN(executiondate) as date, amount
FROM TRANSACTION
WHERE executiondate >= CURRENT_DATE
GROUP BY orderid, descr, amount
ORDER BY descr
