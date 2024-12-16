Create table if not exists SingleOrder
(
    ID                     int primary key,
    descr                  text,
    wt                     boolean,
    amount                 decimal(2),
    planned_execution_date date
);


Create table if not exists RepeatedOrder
(
    ID      int primary key,
    descr   text,
    wt      boolean,
    amount  decimal(2),
    f1      int,
    f2      text,
    f3      text,
    rdd     int,
    rmm     int,
    rlim    boolean,
    rinitdd int,
    rinitmm int,
    rinityy int,
    rfindd  int,
    rfinmm  int,
    rfinyy  int
);
