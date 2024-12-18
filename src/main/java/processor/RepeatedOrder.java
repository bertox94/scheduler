package processor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class RepeatedOrder extends Order {

    private LocalDate plannedExecutionDate;
    private LocalDate effectiveExecutionDate;
    private LocalDate endDate;
    private double amount;
    private int timesRepeated = 0;

    private long f1;
    private String f2;
    private String f3;
    private int rdd;
    private int rmm;
    private boolean rlim;
    private int rinitdd;
    private int rinitmm;
    private int rinityy;
    private int rfindd;
    private int rfinmm;
    private int rfinyy;
    private int year;
    private int month;
    private int day;

    public String getDescr() {
        return descr;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public RepeatedOrder() {
    }

    public RepeatedOrder(HashMap<String, String> map) {

        //repeated = Boolean.parseBoolean(map.get("repeated"));
        descr = map.get("descr");
        wt = Boolean.parseBoolean(map.get("wt"));
        amount = Double.parseDouble(map.get("amount"));
        //if (repeated) {
        f1 = Integer.parseInt(map.get("f1"));
        f2 = map.get("f2");
        f3 = map.get("f3");
        if (!Objects.equals(map.get("rdd"), "$"))
            rdd = Integer.parseInt(map.get("rdd"));
        if (!Objects.equals(map.get("rmm"), "$"))
            rmm = Integer.parseInt(map.get("rmm"));
        if (!Objects.equals(map.get("rinitdd"), "$"))
            rinitdd = Integer.parseInt(map.get("rinitdd"));
        if (!Objects.equals(map.get("rinitmm"), "$"))
            rinitmm = Integer.parseInt(map.get("rinitmm"));
        if (!Objects.equals(map.get("rinityy"), "$"))
            rinityy = Integer.parseInt(map.get("rinityy"));
        rlim = Objects.equals(map.get("rlim"), "limited");
        if (rlim) {
            if (!Objects.equals(map.get("rfindd"), "$"))
                rfindd = Integer.parseInt(map.get("rfindd"));
            if (!Objects.equals(map.get("rfinmm"), "$"))
                rfinmm = Integer.parseInt(map.get("rfinmm"));
            if (!Objects.equals(map.get("rfinyy"), "$"))
                rfinyy = Integer.parseInt(map.get("rfinyy"));
        }
        // }
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setPlannedExecutionDate(LocalDate plannedExecutionDate) {
        this.plannedExecutionDate = plannedExecutionDate;
    }

    public void setEffectiveExecutionDate(LocalDate effectiveExecutionDate) {
        this.effectiveExecutionDate = effectiveExecutionDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTimesRepeated(int timesRepeated) {
        this.timesRepeated = timesRepeated;
    }

    public void setF1(String f1) {
        this.f1 = Integer.parseInt(f1);
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public void setF3(String f3) {
        this.f3 = f3;
    }

    public void setRdd(String rdd) {
        if (!rdd.equals("$"))
            this.rdd = Integer.parseInt(rdd);
    }

    public void setRmm(String rmm) {
        if (!rmm.equals("$"))
            this.rmm = Integer.parseInt(rmm);
    }

    public void setRlim(String rlim) {
        this.rlim = rlim.equals("limited");
    }

    public void setRinitdd(String rinitdd) {
        if (!rinitdd.equals("$"))
            this.rinitdd = Integer.parseInt(rinitdd);
    }

    public void setRinitmm(String rinitmm) {
        if (!rinitmm.equals("$"))
            this.rinitmm = Integer.parseInt(rinitmm);
    }

    public void setRinityy(String rinityy) {
        if (!rinityy.equals("$"))
            this.rinityy = Integer.parseInt(rinityy);
    }

    public void setRfindd(String rfindd) {
        if (!rfindd.equals("$") && rlim)
            this.rfindd = Integer.parseInt(rfindd);
    }

    public void setRfinmm(String rfinmm) {
        if (!rfinmm.equals("$") && rlim)
            this.rfinmm = Integer.parseInt(rfinmm);
    }

    public void setRfinyy(String rfinyy) {
        if (!rfinyy.equals("$") && rlim)
            this.rfinyy = Integer.parseInt(rfinyy);
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

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getTimesRepeated() {
        return timesRepeated;
    }

    public long getF1() {
        return f1;
    }

    public String getF2() {
        return f2;
    }

    public String getF3() {
        return f3;
    }

    public int getRdd() {
        return rdd;
    }

    public int getRmm() {
        return rmm;
    }

    public boolean isRlim() {
        return rlim;
    }

    public int getRinitdd() {
        return rinitdd;
    }

    public int getRinitmm() {
        return rinitmm;
    }

    public int getRinityy() {
        return rinityy;
    }

    public int getRfindd() {
        return rfindd;
    }

    public int getRfinmm() {
        return rfinmm;
    }

    public int getRfinyy() {
        return rfinyy;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public void schedule() {
        //if (repeated) {
        LocalDate dtt;
        long f1 = this.f1 * timesRepeated;
        if ("days".equals(f2)) {
            dtt = LocalDate.of(rinityy, rinitmm, rinitdd);

            dtt = dtt.plusDays(f1);

            plannedExecutionDate = dtt;
            if (rlim)
                endDate = LocalDate.of(rfinyy, rfinmm, rfindd);
        } else if ("months".equals(f2)) {
            dtt = LocalDate.of(rinityy, rinitmm, 1);

            if (rdd > dtt.lengthOfMonth() || "eom".equals(f3)) {
                dtt = dtt.withDayOfMonth(dtt.lengthOfMonth());
            } else {
                dtt = dtt.withDayOfMonth(rdd);
            }

            dtt = dtt.plusMonths(f1);
            plannedExecutionDate = dtt;

            if (rlim) {
                LocalDate enddt = LocalDate.of(rfinyy, rfinmm, 1);
                if (rdd > enddt.lengthOfMonth() || "eom".equals(f3))
                    enddt = enddt.withDayOfMonth(enddt.lengthOfMonth());
                else
                    enddt = enddt.withDayOfMonth(rdd);
                endDate = enddt;
            }
        } else if ("years".equals(f2)) {
            dtt = LocalDate.of(rinityy, rmm, 1);
            LocalDate enddt = LocalDate.of(rfinyy, rmm, 1);

            if (rdd > dtt.lengthOfMonth() || "eom".equals(f3))
                dtt = dtt.withDayOfMonth(dtt.lengthOfMonth());
            else if ("eoy".equals(f3))
                dtt = dtt.withMonth(12).withDayOfMonth(dtt.lengthOfMonth());
            else
                dtt = dtt.withDayOfMonth(rdd);

            dtt = dtt.plusYears(f1);
            plannedExecutionDate = dtt;

            if (rlim) {
                if (rdd > enddt.lengthOfMonth() || "eom".equals(f3))
                    enddt = enddt.withDayOfMonth(enddt.lengthOfMonth());
                else if ("eoy".equals(f3))
                    enddt = enddt.withMonth(12).withDayOfMonth(enddt.lengthOfMonth());
                else
                    enddt = enddt.withDayOfMonth(rdd);
                endDate = enddt;
            }
        }

        setExecutionDate();
        timesRepeated++;
    }


    public LocalDate getPlannedExecutionDate() {
        return plannedExecutionDate;
    }

    public LocalDate getEffectiveExecutionDate() {
        return effectiveExecutionDate;
    }

    public double getAmount() {
        return amount;
    }


    public boolean isExpired() {
        return rlim && effectiveExecutionDate.isAfter(endDate);
    }

}
