package com.example.scheduler;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.example.scheduler.MainController.connection;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) throws SQLException, IOException {

        initialize_DB_connection();
        SpringApplication.run(SchedulerApplication.class, args);
        LoggerFactory.getLogger(SchedulerApplication.class).info("Database online.");
        LoggerFactory.getLogger(SchedulerApplication.class).info("Initialization completed.");

    }


    public static void initialize_DB_connection() throws SQLException, IOException {

        String jdbcURL = "jdbc:h2:./h2"; //./h2.mv.db   jdbc:h2:file:./h2
        String username = "sa";
        String password = "1234";

        jdbcURL = "jdbc:postgresql://pg-1c4a5739-mail-a916.e.aivencloud.com:26114/defaultdb?ssl=require";
        username = "avnadmin";
        password = "";

        connection = DriverManager.getConnection(jdbcURL, username, password);

        System.out.println("Connected to Postgres remote database.");

        String sql = Files.readString(Paths.get(".\\queries\\initializeDatabase.sql"));
        ;
        Statement statement = connection.createStatement();
        statement.execute(sql);

    }
}
