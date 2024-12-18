package com.example.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import processor.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

;

@Controller
public class MainController {

    static Connection connection;
    static final ObjectMapper mapper = new ObjectMapper();

    public List<Order> allOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM SingleOrder ");

        while (rs.next()) {
            SingleOrder order = new SingleOrder();
            order.setDescr(rs.getString("descr"));
            order.setWt(rs.getBoolean("wt"));
            order.setAmount(rs.getDouble("amount"));
            order.setYear(Integer.parseInt(rs.getString("year")));
            order.setMonth(Integer.parseInt(rs.getString("month")));
            order.setDay(Integer.parseInt(rs.getString("day")));
            orders.add(order);
        }

        return orders;
    }

    @ResponseBody
    @PostMapping(path = "/orders")
    public String orders() throws SQLException, JsonProcessingException {
        List<Order> orders = allOrders();
        for (Order order : orders) {
            order.schedule();
        }
        return mapper.writeValueAsString(orders);
    }

    public SingleOrder createSingleOrder(int id) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM SingleOrder WHERE id = " + id);
        return createSingleOrder(rs);
    }

    private static SingleOrder createSingleOrder(ResultSet rs) throws SQLException {
        if (rs.next()) {
            SingleOrder order = new SingleOrder();
            order.setDescr(rs.getString("descr"));
            order.setWt(rs.getBoolean("wt"));
            order.setAmount(rs.getDouble("amount"));
            order.setYear(Integer.parseInt(rs.getString("year")));
            order.setMonth(Integer.parseInt(rs.getString("month")));
            order.setDay(Integer.parseInt(rs.getString("day")));
            return order;
        }
        return null;
    }

    @ResponseBody
    @PostMapping(path = "/schedule")
    public String schedule(@RequestParam String data) throws JsonProcessingException, SQLException {

        Order order = createSingleOrder(Integer.parseInt(data));
        order.schedule();
        if (order.getEffectiveExecutionDate().isBefore(LocalDate.now()))
            return "It is in the past";
        return order.getEffectiveExecutionDate().toString();

/*
        RepeatedOrder order = mapper.readValue(data, RepeatedOrder.class);
        do order.schedule();
        while (order.isExpired() || order.getEffectiveExecutionDate().isBefore(LocalDate.now()));
        if (order.getEffectiveExecutionDate().isBefore(LocalDate.now()))
            return "It is in the past";
        if (order.isExpired())
            return "It is expired";
        return order.getEffectiveExecutionDate().toString();
*/

    }

    @ResponseBody
    @PostMapping(path = "/preview")
    public String preview(@RequestParam String data) throws IOException, SQLException {

        LocalDate endDate = LocalDate.parse(data, DateTimeFormatter.ofPattern("y-M-d"));

        Statement stmt = connection.createStatement();
        stmt.executeQuery("SELECT public.generate_order_occurrences('" + endDate + "');");

        ResultSet rs = stmt.executeQuery(Files.readString(Paths.get(".\\queries\\getSummary.sql")));
        List<Transaction> summary = new ArrayList<>();

        while (rs.next()) {
            summary.add(new Transaction(rs.getLong("orderid"),
                    rs.getString("descr"),
                    rs.getDate("date").toString(),
                    rs.getDouble("amount")));
        }

        Map<String, List<Transaction>> map = new HashMap<>();
        map.put("summary", summary);


        stmt = connection.createStatement();
        rs = stmt.executeQuery("SELECT * FROM public.TRANSACTION ORDER BY executiondate");
        List<Transaction> computed = new ArrayList<>();

        while (rs.next()) {
            computed.add(new Transaction(rs.getLong("orderid"),
                    rs.getString("descr"),
                    rs.getDate("executionDate").toString(),
                    rs.getDouble("amount")));
        }

        map.put("computed", computed);
        return mapper.writeValueAsString(map);

    }

    private static Result linRegression(List<Double> allDatesBalance) {
        double slope;
        double intercept;

        Double[] x = new Double[allDatesBalance.size()];
        Double[] y = new Double[allDatesBalance.size()];

        for (int i = 0; i < allDatesBalance.size(); i++) {
            x[i] = (double) i;
            y[i] = allDatesBalance.get(i);
        }

        int n = x.length;

        // first pass
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx += x[i];
            sumx2 += x[i] * x[i];
            sumy += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        slope = xybar / xxbar;
        intercept = ybar - slope * xbar;
        return new Result(slope, intercept);
    }

    private record Result(double slope, double intercept) {
    }

    @ResponseBody
    @PostMapping(path = "/addnewsingle")
    public String addnewsingle(@RequestParam String data) throws IOException, SQLException {

        SingleOrder order = mapper.readValue(data, SingleOrder.class);

        Statement stmt = connection.createStatement();
        String s = "";

        StringJoiner sj = new StringJoiner(", ", "", ");");
        sj.add("(" + Files.readString(Paths.get(".\\queries\\getFirstId.sql")) + ")")
                .add('\'' + order.getDescr() + '\'')
                .add(String.valueOf(order.isWt()))
                .add(String.valueOf(order.getAmount()))
                .add(String.valueOf(order.getYear()))
                .add(String.valueOf(order.getMonth()))
                .add(String.valueOf(order.getDay()));

        stmt.executeUpdate("INSERT INTO public.singleOrder VALUES (" + sj);
        return "OK";
    }


    @ResponseBody
    @PostMapping(path = "/addnewrepeated")
    public String addnewrepeated(@RequestParam String data) throws IOException, SQLException {

        RepeatedOrder o = mapper.readValue(data, RepeatedOrder.class);

        Statement stmt = connection.createStatement();
        StringJoiner sj = new StringJoiner(",", "", ");");
        sj.add("(" + Files.readString(Paths.get(".\\queries\\getFirstId.sql")) + ")")
                .add('\'' + o.getDescr() + '\'')
                .add(String.valueOf(o.isWt()))
                .add(String.valueOf(o.getAmount()))
                .add(String.valueOf(o.getF1()))
                .add('\'' + o.getF2() + '\'')
                .add('\'' + o.getF3() + '\'')
                .add(String.valueOf(o.getRdd()))
                .add(String.valueOf(o.getRmm()))
                .add(String.valueOf(o.isRlim()))
                .add(String.valueOf(o.getRinitdd()))
                .add(String.valueOf(o.getRinitmm()))
                .add(String.valueOf(o.getRinityy()))
                .add(String.valueOf(o.getRfindd()))
                .add(String.valueOf(o.getRfinmm()))
                .add(String.valueOf(o.getRfinyy()));

        stmt.executeUpdate("INSERT INTO public.repeatedOrder VALUES (" + sj);
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/get")
    public String get(@RequestParam String _id) throws SQLException {
        String resp = "";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT encoded " +
                        " FROM public.orders " +
                        " WHERE id = " + _id + ";");

        if (rs.next())
            resp = rs.getString(1);

        return resp;
    }

    @ResponseBody
    @PostMapping(path = "/delete")
    public String delete(@RequestParam String data) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("DELETE FROM public.orders WHERE id = " + data + ";");
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/duplicate")
    public String duplicate(@RequestParam String data) throws IOException, SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("""
                WITH     duplicated_record AS (
                                        SELECT name, email, position, salary
                                        FROM employees
                                        WHERE id = 1  -- Specify the ID of the record you want to duplicate
                                )
                                INSERT INTO employees (name, email, position, salary)
                                SELECT name, email, position, salary
                                FROM duplicated_record;""");

        return "OK";
    }
}
