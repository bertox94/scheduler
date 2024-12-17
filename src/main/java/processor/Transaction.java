package processor;

import java.time.LocalDate;

public class Transaction {
    public long orderid;
    public String descr;
    public LocalDate executionDate;
    public double amount;

    public Transaction(long orderid, String descr, LocalDate executionDate, double amount) {
        this.orderid = orderid;
        this.descr = descr;
        this.executionDate = executionDate;
        this.amount = amount;
    }

}
