package processor;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Scheduler {


    public static List<Transaction> preview(List<Order> orders, LocalDate endDate) {
        List<Transaction> records = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Order order : orders) {
            order.schedule();
            if (order.isRepeated()) {
                while (!order.getEffectiveExecutionDate().isAfter(endDate) &&
                        !order.isExpired()) {
                    if (!order.getEffectiveExecutionDate().isBefore(today))
                        records.add(new Transaction(order.getDescr(), order.getPlannedExecutionDate(), order.getEffectiveExecutionDate(), order.getAmount()));
                    order.schedule();
                }
            } else {
                if (!order.getEffectiveExecutionDate().isAfter(endDate) &&
                        !order.getEffectiveExecutionDate().isBefore(today))
                    records.add(new Transaction(order.getDescr(), order.getPlannedExecutionDate(), order.getEffectiveExecutionDate(), order.getAmount()));
            }
        }
        return records;
    }
}
