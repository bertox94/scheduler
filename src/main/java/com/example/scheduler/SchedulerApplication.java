package com.example.scheduler;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.fasterxml.jackson.datatype.jsr310.*;

import static com.example.scheduler.MainController.connection;
import static com.example.scheduler.MainController.mapper;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) throws SQLException, IOException {

        initialize_DB_connection();
        mapper.registerModule(new JavaTimeModule());
        SpringApplication.run(SchedulerApplication.class, args);
        LoggerFactory.getLogger(SchedulerApplication.class).info("Database online.");
        LoggerFactory.getLogger(SchedulerApplication.class).info("Initialization completed.");

    }


    public static void initialize_DB_connection() throws SQLException, IOException {

        String jdbcURL = "jdbc:postgresql://localhost:5432/postgres?ssl=require";//"jdbc:postgresql://pg-1c4a5739-mail-a916.e.aivencloud.com:26114/defaultdb?ssl=require";
        String username = "postgres";
        String password = "admin";

        connection = DriverManager.getConnection(jdbcURL, username, password);

        System.out.println("Connected to Postgres remote database.");

        String sql = Files.readString(Paths.get(".\\queries\\initializeDatabase.sql"));
        Statement statement = connection.createStatement();
        statement.execute(sql);

    }
}
