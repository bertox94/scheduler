package processor;

import java.time.LocalDate;

public class Transaction {
    public long orderid;
    public String descr;
    public String executionDate;
    public double amount;

    public Transaction(long orderid, String descr, String executionDate, double amount) {
        this.orderid = orderid;
        this.descr = descr;
        this.executionDate = executionDate;
        this.amount = amount;
    }

}
