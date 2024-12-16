package processor;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Order {

    protected int id;
    protected String descr;
    protected boolean wt;
    protected double amount;
    protected LocalDate plannedExecutionDate;
    protected LocalDate effectiveExecutionDate;

    public void schedule() {
    }

    void setExecutionDate() {
        LocalDate dtt = plannedExecutionDate;
        if (wt)
            while (dtt.getDayOfWeek() == DayOfWeek.SATURDAY ||
                   dtt.getDayOfWeek() == DayOfWeek.SUNDAY) {
                dtt = dtt.plusDays(1);
            }
        effectiveExecutionDate = dtt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public void setWt(boolean wt) {
        this.wt = wt;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPlannedExecutionDate(LocalDate plannedExecutionDate) {
        this.plannedExecutionDate = plannedExecutionDate;
    }

    public void setEffectiveExecutionDate(LocalDate effectiveExecutionDate) {
        this.effectiveExecutionDate = effectiveExecutionDate;
    }

    public int getId() {
        return id;
    }

    public String getDescr() {
        return descr;
    }

    public boolean isWt() {
        return wt;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPlannedExecutionDate() {
        return plannedExecutionDate;
    }

    public LocalDate getEffectiveExecutionDate() {
        return effectiveExecutionDate;
    }
}
