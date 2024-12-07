package processor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class Order {

    private int id;
    private final boolean repeated;
    private final String descr;
    private final boolean wt;
    private LocalDate plannedExecutionDate;
    private LocalDate effectiveExecutionDate;
    private LocalDate endDate;
    private final double amount;
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

    public String getDescr() {
        return descr;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Order(HashMap<String, String> map) {

        repeated = Boolean.parseBoolean(map.get("repeated"));
        descr = map.get("descr");
        wt = Boolean.parseBoolean(map.get("wt"));
        amount = Double.parseDouble(map.get("amount"));
        if (repeated) {
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
        } else
            plannedExecutionDate = LocalDate.of(Integer.parseInt(map.get("year")), Integer.parseInt(map.get("month")), Integer.parseInt(map.get("day")));
    }

    public void schedule() {
        if (repeated) {
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
        }
        setExecutionDate();
        timesRepeated++;
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

    public LocalDate getPlannedExecutionDate() {
        return plannedExecutionDate;
    }

    public LocalDate getEffectiveExecutionDate() {
        return effectiveExecutionDate;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public boolean isExpired() {
        return rlim && effectiveExecutionDate.isAfter(endDate);
    }
}
