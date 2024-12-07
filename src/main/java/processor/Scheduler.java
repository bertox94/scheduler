package processor;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Scheduler {


    public static List<Transaction> preview(List<Order> orders, LocalDate endDate) {
        List<Transaction> records = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < orders.size() && !orders.get(i).isRepeated(); i++) {
            Order order = orders.get(i);
            order.schedule();
            LocalDate exDate=order.getEffectiveExecutionDate();
            if (!exDate.isAfter(endDate) && !exDate.isBefore(today))
                records.add(new Transaction(order.getDescr(), order.getPlannedExecutionDate(), order.getEffectiveExecutionDate(), order.getAmount()));
            orders.remove(i);
        }


        for (int i = 0; i < orders.size(); ) {
            Order order = orders.get(i);
            order.schedule();
            LocalDate exDate=order.getEffectiveExecutionDate();
            while (!exDate.isAfter(endDate) &&
                    //!order.effectiveExecutionDate.isBefore(today) &&
                    !order.isExpired()) {
                Transaction transaction = new Transaction(order.getDescr(), order.getPlannedExecutionDate(), order.getEffectiveExecutionDate(), order.getAmount());
                records.add(transaction);
                order.schedule();
            }
            orders.remove(i);
        }

        return records;
    }
}
