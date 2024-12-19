package com.example.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class MainController {

    static Connection connection;
    static final ObjectMapper mapper = new ObjectMapper();
    static AtomicInteger id_user = new AtomicInteger(0);

    static void initialize() throws IOException, SQLException {
        String jdbcURL = "jdbc:postgresql://localhost:5432/postgres?ssl=require";
        jdbcURL = "jdbc:postgresql://pg-1c4a5739-mail-a916.e.aivencloud.com:26114/defaultdb?ssl=require";
        String username = "avnadmin";
        String password = "";

        connection = DriverManager.getConnection(jdbcURL, username, password);
        mapper.registerModule(new JavaTimeModule());

        String sql = Files.readString(Paths.get(".\\queries\\initializeDatabase.sql"));
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    @ResponseBody
    @PostMapping(path = "/preview")
    public String preview(@RequestParam String data) throws IOException, SQLException {

        LocalDate endDate = LocalDate.parse(data, DateTimeFormatter.ofPattern("y-M-d"));
        endDate = endDate.getDayOfMonth() > endDate.lengthOfMonth() ? endDate.withDayOfMonth(endDate.lengthOfMonth()) : endDate;
        long iduser = System.currentTimeMillis() * 10 + id_user.incrementAndGet();
        PreparedStatement stmt = connection.prepareStatement("SELECT public.generateOrderOccurrences(?,?);");
        stmt.setLong(1, endDate.toEpochDay() * 86400);
        stmt.setLong(2, iduser);

        stmt.executeQuery();

        ResultSet rs = connection.createStatement().executeQuery(Files
                .readString(Paths.get(".\\queries\\getSummary.sql"))
                .replace("?", String.valueOf(iduser)));
        List<Transaction> summary = new ArrayList<>();

        while (rs.next()) {
            summary.add(new Transaction(rs.getLong("orderid"),
                    rs.getString("descr"),
                    rs.getDate("date"),
                    rs.getDouble("amount")));
        }

        Map<String, List<Transaction>> map = new HashMap<>();
        map.put("summary", summary);


        rs = connection.createStatement().executeQuery("SELECT * FROM public.TRANSACTION ORDER BY executiondate");
        List<Transaction> computed = new ArrayList<>();

        while (rs.next()) {
            computed.add(new Transaction(rs.getLong("orderid"),
                    rs.getString("descr"),
                    rs.getDate("executionDate"),
                    rs.getDouble("amount")));
        }

        map.put("computed", computed);

        stmt = connection.prepareStatement("DELETE FROM public.TRANSACTION WHERE iduser = ?;");
        stmt.setLong(1, iduser);
        stmt.executeUpdate();

        id_user.decrementAndGet();
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

        Map<String, String> order = mapper.readValue(data, Map.class);

        PreparedStatement stmt = connection
                .prepareStatement("INSERT INTO public.singleOrder VALUES ((SELECT public.getFirstId()),?,?,?,?);");
        stmt.setString(1, order.get("descr"));
        stmt.setString(2, order.get("wt"));
        stmt.setString(3, order.get("amount"));
        stmt.setDate(4, Date.valueOf(order.get("date")));

        stmt.executeUpdate();
        return "OK";
    }


    @ResponseBody
    @PostMapping(path = "/addnewrepeated")
    public String addnewrepeated(@RequestParam String data) throws IOException, SQLException {

        Map<String, String> order = mapper.readValue(data, Map.class);

        PreparedStatement stmt = connection
                .prepareStatement("INSERT INTO public.repeatedOrder VALUES (" +
                                  "(SELECT public.getFirstId()),?,?,?,?,?,?,?,?,?,?,?,?,?);");
        stmt.setString(1, order.get("descr"));
        stmt.setBoolean(2, order.get("wt").equalsIgnoreCase("true"));
        stmt.setString(3, order.get("amount"));
        stmt.setInt(4, Integer.parseInt(order.get("f1")));
        stmt.setString(5, order.get("f2"));
        stmt.setString(6, order.get("f3"));
        stmt.setBoolean(9, order.get("rlim").equalsIgnoreCase("limited"));
        stmt.setInt(10, order.get("rinitdd").equals("$") ? 0 : Integer.parseInt(order.get("rinitdd")));
        stmt.setInt(11, order.get("rinitmm").equals("$") ? 0 : Integer.parseInt(order.get("rinitmm")));
        stmt.setInt(12, Integer.parseInt(order.get("rinityy")));
        stmt.setInt(13, order.get("rfindd").equals("$") ? 0 : Integer.parseInt(order.get("rfindd")));
        stmt.setInt(14, order.get("rfinmm").equals("$") ? 0 : Integer.parseInt(order.get("rfinmm")));
        stmt.setInt(15, Integer.parseInt(order.get("rfinyy")));

        stmt.executeUpdate();
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/delete")
    public String delete(@RequestParam String data) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("DELETE FROM public.repeatedOrder WHERE id = " + data + ";");
        stmt.executeUpdate("DELETE FROM public.singleOrder WHERE id = " + data + ";");
        return "OK";
    }

    @ResponseBody
    @PostMapping(path = "/duplicate")
    public String duplicate(@RequestParam String data) throws IOException, SQLException {
        Statement stmt = connection.createStatement();
        String s = """
                WITH duplicated_record AS (SELECT descr,
                                                  wt,
                                                  amount,
                                                  f1,
                                                  f2,
                                                  f3,
                                                  rdd,
                                                  rmm,
                                                  rlim,
                                                  rinitdd,
                                                  rinitmm,
                                                  rinityy,
                                                  rfindd,
                                                  rfinmm,
                                                  rfinyy
                                           FROM public.repeatedorder
                                           WHERE id = (%s) -- Specify the ID of the record you want to duplicate
                )
                INSERT
                INTO public.repeatedorder
                SELECT (SELECT public.getfirstid()), *
                FROM duplicated_record;""".formatted(data);
        stmt.executeUpdate(s);

        s = """
                WITH duplicated_record AS (SELECT descr,
                                                  wt,
                                                  amount,
                                                  plannedexecutiondate
                                           FROM public.singleorder
                                           WHERE id = (%s) -- Specify the ID of the record you want to duplicate
                )
                INSERT
                INTO public.singleorder
                SELECT (SELECT public.getfirstid()), *
                FROM duplicated_record;""".formatted(data);
        stmt.executeUpdate(s);

        return "OK";
    }
}
