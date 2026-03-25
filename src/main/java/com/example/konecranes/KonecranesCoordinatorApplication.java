package com.example.konecranes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot coordinator application bootstrap class.
 */
@SpringBootApplication
public class KonecranesCoordinatorApplication {

    /**
     * Starts the coordinator Spring context.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KonecranesCoordinatorApplication.class, args);
    }
}
