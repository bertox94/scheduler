SELECT orderid, descr, MIN(executiondate) as date, amount
FROM TRANSACTION
WHERE executiondate >= CURRENT_DATE AND iduser = ?
GROUP BY orderid, descr, amount
ORDER BY descr
