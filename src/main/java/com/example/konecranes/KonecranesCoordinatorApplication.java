package com.example.konecranes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the coordinator application.
 *
 * Starts the coordinator context, including REST endpoints,
 * TCP server components, schedulers, and application services.
 */
@SpringBootApplication
public class KonecranesCoordinatorApplication {

    /**
     * Starts the coordinator Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KonecranesCoordinatorApplication.class, args);
    }
}