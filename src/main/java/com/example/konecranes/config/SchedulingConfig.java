package com.example.konecranes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring scheduled task execution for coordinator jobs.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
