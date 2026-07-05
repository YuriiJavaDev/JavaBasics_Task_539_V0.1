package com.yurii.pavlenko.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for component scanning.
 */
@Configuration
@ComponentScan(basePackages = "com.yurii.pavlenko")
public class AppConfig {
}