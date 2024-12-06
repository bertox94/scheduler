package com.example.scheduler;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.example.scheduler.MainController.connection;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) throws SQLException {

        initialize_DB_connection();
        SpringApplication.run(SchedulerApplication.class, args);
        LoggerFactory.getLogger(SchedulerApplication.class).info("Database online.");
        LoggerFactory.getLogger(SchedulerApplication.class).info("Initialization completed.");

    }


    public static void initialize_DB_connection() throws SQLException {

        String jdbcURL = "jdbc:h2:./h2"; //./h2.mv.db   jdbc:h2:file:./h2
        String username = "sa";
        String password = "1234";

        connection = DriverManager.getConnection(jdbcURL, username, password);

        System.out.println("Connected to H2 embedded database.");

        String sql = "Create table if not exists ORDERS (ID int primary key, encoded varchar(250));";
        Statement statement = connection.createStatement();
        statement.execute(sql);

    }
}
