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

        MainController.initialize();
        SpringApplication.run(SchedulerApplication.class, args);

    }
}
