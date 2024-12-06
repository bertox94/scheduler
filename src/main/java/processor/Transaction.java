package processor;

import java.time.LocalDate;

public class Transaction {
    public String descr;
    public LocalDate plannedExecutionDate;
    public LocalDate effectiveExecutionDate;
    public double amount;

    public Transaction(String descr, LocalDate plannedExecutionDate, LocalDate effectiveExecutionDate, double amount) {
        this.descr = descr;
        this.plannedExecutionDate = plannedExecutionDate;
        this.effectiveExecutionDate = effectiveExecutionDate;
        this.amount = amount;
    }

    public LocalDate getEffectiveExecutionDate() {
        return effectiveExecutionDate;
    }
}
