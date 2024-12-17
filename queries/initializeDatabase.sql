Create table if not exists SingleOrder
(
    ID                     int primary key,
    descr                  text,
    wt                     boolean,
    amount                 numeric(10, 2),
    planned_execution_date date
);


create table if not exists repeatedorder
(
    id      integer not null
        primary key,
    descr   text,
    wt      boolean,
    amount  numeric(10, 2),
    f1      integer,
    f2      text,
    f3      text,
    rdd     integer,
    rmm     integer,
    rlim    boolean,
    rinitdd integer,
    rinitmm integer,
    rinityy integer,
    rfindd  integer,
    rfinmm  integer,
    rfinyy  integer
);

create table if not exists transaction
(
    id            integer not null
        constraint orders_pk
            primary key,
    orderid       integer,
    descr         text,
    executiondate date,
    amount        numeric(10,2)
);