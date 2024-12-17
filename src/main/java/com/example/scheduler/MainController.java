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

@Controller
public class MainController {

    static Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();

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
    public String preview(@RequestParam String data) throws JsonProcessingException, SQLException {

        LocalDate endDate = LocalDate.parse(data.substring(0, data.indexOf('\n')), DateTimeFormatter.ofPattern("y-M-d"));//LocalDate.of(Integer.parseInt(mao.get("year")),Integer.parseInt(mao.get("month")),Integer.parseInt(mao.get("day")));
        double balance = Double.parseDouble(data.substring(data.indexOf('\n') + 1));

        List<Order> orderList = new ArrayList<>();

        String[] orders = orders().split("\n");

        for (String line : orders) {
            if (!line.isEmpty())
                orderList.add(new Order()); //mapper.readValue(line, Map.class)))
        }

        List<Transaction> records = Scheduler.preview(orderList, endDate);
        records.sort(Comparator.comparing(Transaction::getEffectiveExecutionDate));

        StringJoiner resp = new StringJoiner(",", "{", "}");
        resp.add("\"enddate\":\"" + endDate + "\"");
        resp.add("\"initialbal\":\"" + balance + "\"");

        double bal = balance;
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (Transaction record : records) {
            bal += record.amount;
            StringJoiner sjj = new StringJoiner(",", "{", "}");
            sjj.add("\"executiondate\":\"" + record.effectiveExecutionDate + "\"");
            sjj.add("\"planneddate\":\"" + record.plannedExecutionDate + "\"");
            sjj.add("\"descr\":\"" + record.descr + "\"");
            sjj.add("\"amount\":\"" + record.amount + "\"");
            sjj.add("\"balance\":\"" + bal + "\"");
            sj.add(sjj.toString());
        }

        resp.add("\"html\":" + sj);

        LocalDate today = LocalDate.now();
        List<LocalDate> allDates = new ArrayList<>();
        List<Double> allDatesBalance = new ArrayList<>();
        List<Double> balancePerTransaction = new ArrayList<>();

        while (!today.isAfter(endDate)) {
            allDates.add(today);
            today = today.plusDays(1);
        }

        bal = balance;
        for (LocalDate date : allDates) {
            double tot = 0;
            for (Transaction record : records) {
                if (record.effectiveExecutionDate.equals(date)) {
                    tot += record.amount;
                }
            }
            bal += tot;
            allDatesBalance.addLast(bal);
        }

        bal = balance;
        for (Transaction record : records) {
            bal += record.amount;
            balancePerTransaction.addLast(bal);
        }

        sj = new StringJoiner(",", "[", "]");
        for (LocalDate date : allDates) {
            sj.add("\"" + date + "\"");
        }
        resp.add("\"arr1\":" + sj);

        sj = new StringJoiner(",", "[", "]");
        for (Double bala : allDatesBalance) {
            sj.add("\"" + bala + "\"");
        }
        resp.add("\"arr2\":" + sj);


        Result result = linRegression(allDatesBalance);
        resp.add("\"m\":\"" + result.slope() + "\"");
        resp.add("\"q\":\"" + result.intercept() + "\"");

        sj = new StringJoiner(",", "[", "]");
        for (long i = 0; i < allDatesBalance.size(); i++) {
            sj.add("\"" + String.format(Locale.US, "%.2f", (result.slope() * i + result.intercept())) + "\"");
        }
        resp.add("\"arr3\":" + sj);


        sj = new StringJoiner(",", "[", "]");
        for (Transaction transaction : records) {
            sj.add("\"" + transaction.effectiveExecutionDate + "\"");
        }
        resp.add("\"arr4\":" + sj);

        sj = new StringJoiner(",", "[", "]");
        for (Transaction transaction : records) {
            sj.add("\"" + transaction.amount + "\"");
        }
        resp.add("\"arr5\":" + sj);

        sj = new StringJoiner(",", "[", "]");
        for (Double bala : balancePerTransaction) {
            sj.add("\"" + bala + "\"");
        }
        resp.add("\"arr6\":" + sj);

        return resp.toString();
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
        String s = String.format("""
                        INSERT INTO singleOrder
                        VALUES((%s), '%s', %s, %s, %s, %s, %s)
                        """,
                Files.readString(Paths.get(".\\queries\\getFirstId.sql")),
                order.getDescr(),
                order.isWt(),
                order.getAmount(),
                order.getYear(),
                order.getMonth(),
                order.getDay());
        stmt.executeUpdate(s);
        return "OK";
    }


    @ResponseBody
    @PostMapping(path = "/addnewrepeated")
    public String addnewrepeated(@RequestParam String data) throws IOException {

        RepeatedOrder o = mapper.readValue(data, RepeatedOrder.class);
        try {

            String sqlQuery = Files.readString(Paths.get(".\\queries\\getFirstId.sql"));
            Statement stmt = connection.createStatement();
            StringJoiner sj = new StringJoiner(",", "", ");");
            sj.add('\'' + o.getDescr() + '\'')
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
            stmt.executeUpdate(
                    "INSERT INTO public.repeatedorder " +
                            "VALUES((" + sqlQuery + "), " +
                            sj);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "KO";
        }
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/get")
    public String get(@RequestParam String _id) {
        String resp = "";
        try {

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT encoded " +
                            " FROM public.orders " +
                            " WHERE id = " + _id + ";");

            if (rs.next())
                resp = rs.getString(1);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "KO";
        }
        return resp;
    }

    @ResponseBody
    @PostMapping(path = "/delete")
    public String delete(@RequestParam String data) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM public.orders WHERE id = " + data + ";");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "KO";
        }
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/duplicate")
    public String duplicate(@RequestParam String data) {
        try {
            Statement stmt = connection.createStatement();

            String _SUB_Q_ID = " (  SELECT ROW_NUMBER " +
                    "               FROM (" +
                    "                  SELECT ROW_NUMBER() OVER (ORDER BY id) AS ROW_NUMBER, id " +
                    "                  FROM ( " +
                    "                      SELECT id    " +
                    "                      FROM orders " +
                    "                      UNION ALL  " +
                    "                      SELECT COALESCE(MAX(id),2) AS id   " +
                    "                      FROM orders         " +
                    "                  ) AS sub1 " +
                    "               ) AS sub2       " +
                    "               WHERE ROW_NUMBER != id      " +
                    "               LIMIT 1) ";

            stmt.executeUpdate(" INSERT INTO orders (id, encoded)" +
                    "SELECT" + _SUB_Q_ID + """
                    , REGEXP_REPLACE(encoded, '"id":"[0-9]*"', '"id":"' || """ + _SUB_Q_ID + """
                    || '"' )
                    FROM orders
                    WHERE id = """ + data + ";");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "KO";
        }
        return "OK";
    }
}
