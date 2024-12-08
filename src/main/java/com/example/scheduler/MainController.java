package com.example.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import processor.Order;
import processor.Scheduler;
import processor.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class MainController {

    static Connection connection;
    private ObjectMapper mapper = new ObjectMapper();

    @ResponseBody
    @PostMapping(path = "/orders")
    public String orders() {
        StringBuilder data = new StringBuilder();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT encoded FROM public.orders " +
                    " order by substring(encoded from (locate('\"descr\":', encoded))) asc ;");

            while (rs.next()) {
                data.append(rs.getString(1)).append("\n");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return data.toString();
    }

    @ResponseBody
    @PostMapping(path = "/schedule")
    public String schedule(@RequestParam String data) throws JsonProcessingException {

        Map<String, String> mao = mapper.readValue(data, Map.class);
        Order order = new Order(new HashMap<>(mao));

        do order.schedule();
        while (order.isExpired() || order.getEffectiveExecutionDate().isBefore(LocalDate.now()));

        LocalDate exDate = order.getEffectiveExecutionDate();
        if (exDate.isBefore(LocalDate.now()) || order.isExpired())
            return "It is in the past";
        else
            return exDate.toString();
    }

    @ResponseBody
    @PostMapping(path = "/preview")
    public String preview(@RequestParam String data) throws JsonProcessingException {

        LocalDate endDate = LocalDate.parse(data.substring(0, data.indexOf('\n')), DateTimeFormatter.ofPattern("y-M-d"));//LocalDate.of(Integer.parseInt(mao.get("year")),Integer.parseInt(mao.get("month")),Integer.parseInt(mao.get("day")));
        double balance = Double.parseDouble(data.substring(data.indexOf('\n') + 1));

        List<Order> orderList = new ArrayList<>();

        String[] orders = orders().split("\n");

        for (String line : orders) {
            if (!line.isEmpty())
                orderList.add(new Order(new HashMap<String, String>(mapper.readValue(line, Map.class))));
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
    @PostMapping(path = "/addnew")
    public String addnew(@RequestParam String data) {
        try {
            String _SUB_Q_ID = " (  SELECT ROW_NUMBER " +
                    "               FROM (" +
                    "                  SELECT ROW_NUMBER() OVER (ORDER BY id) as ROW_NUMBER, id " +
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
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO public.orders (id, encoded) " +
                            "VALUES(" + _SUB_Q_ID + ", '{\"id\":'|| '\"' || " + _SUB_Q_ID + "|| '\"," + data.substring(1) + "');");
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
