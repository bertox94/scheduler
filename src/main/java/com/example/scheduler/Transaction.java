package com.example.scheduler;

import java.sql.Date;

public class Transaction {
    public long orderid;
    public String descr;
    public String executionDate;
    public String amount;

    public Transaction(long orderid, String descr, Date executionDate, Double amount) {
        this.orderid = orderid;
        this.descr = descr;
        this.executionDate = executionDate.toString();
        this.amount = amount.toString();
    }

}
