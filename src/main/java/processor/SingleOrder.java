package processor;

import java.time.LocalDate;

public class SingleOrder extends Order{
    private int year;
    private int month;
    private int day;

    public void schedule(){
        plannedExecutionDate = LocalDate.of(year, month, day);
        setExecutionDate();
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

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}
