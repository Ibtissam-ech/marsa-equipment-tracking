package com.marsamaroc.equipment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class EquipmentTrackingApplication {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("Marsa Maroc Equipment Tracking System");
        System.out.println("Application Started Successfully!");
        System.out.println("==========================================");
        SpringApplication.run(EquipmentTrackingApplication.class, args);
    }
}