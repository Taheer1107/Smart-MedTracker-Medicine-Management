package com.medtracker;

import com.medtracker.view.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class
 * Integrates Spring Boot with JavaFX
 * Entry point for Smart MedTracker application
 */
@SpringBootApplication
@EnableScheduling
public class MedTrackerApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private LoginView loginView;

    public static void main(String[] args) {
        // Launch JavaFX application
        Application.launch(MedTrackerApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        // Initialize Spring Boot context
        springContext = SpringApplication.run(MedTrackerApplication.class);
        loginView = springContext.getBean(LoginView.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Show login view
        loginView.show(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        // Close Spring context when application closes
        springContext.close();
        Platform.exit();
    }
}