package com.medtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Web-only entry point - runs the REST API + web app in Chrome without JavaFX.
 * Use: mvn spring-boot:run
 */
@SpringBootApplication
@EnableScheduling
public class MedTrackerWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedTrackerWebApplication.class, args);
    }
}
