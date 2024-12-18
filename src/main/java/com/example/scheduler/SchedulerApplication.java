package com.example.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) throws SQLException, IOException {

        MainController.initialize();
        SpringApplication.run(SchedulerApplication.class, args);

    }
}
