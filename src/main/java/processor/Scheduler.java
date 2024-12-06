package processor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Scheduler {


    public static List<Transaction> preview(List<Order> orders, LocalDate endDate) {
        List<Transaction> records = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < orders.size() && !orders.get(i).repeated; i++) {
            Order order = orders.get(i);
            order.schedule();
            if (!order.effectiveExecutionDate.isAfter(endDate) && !order.effectiveExecutionDate.isBefore(today))
                records.add(new Transaction(order.descr, order.plannedExecutionDate, order.effectiveExecutionDate, order.amount));
            orders.remove(i);
        }


        for (int i = 0; i < orders.size(); ) {
            Order order = orders.get(i);
            order.schedule();
            while (!order.effectiveExecutionDate.isAfter(endDate) &&
                    !order.effectiveExecutionDate.isBefore(today) &&
                    !order.isExpired()) {
                Transaction transaction = new Transaction(order.descr, order.plannedExecutionDate, order.effectiveExecutionDate, order.amount);
                records.add(transaction);
                order.reschedule();
            }
            orders.remove(i);
        }

        return records;
    }
}
