package com.marsamaroc.equipment.config;

import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner init(UserRepository userRepo, EquipmentRepository equipmentRepo) {
        return args -> {
            if (userRepo.count() == 0) {
                User admin = new User(); admin.setUsername("admin"); admin.setPassword("password");
                admin.setFullName("Administrateur"); admin.setEmail("admin@marsa.ma");
                admin.setRole("ADMIN"); admin.setDepartment("IT");
                userRepo.save(admin);
                
                User tech = new User(); tech.setUsername("tech001"); tech.setPassword("password");
                tech.setFullName("Technicien"); tech.setEmail("tech@marsa.ma");
                tech.setRole("TECHNICIEN"); tech.setDepartment("Maintenance");
                userRepo.save(tech);
                
                Equipment eq1 = new Equipment(); eq1.setModel("Dell Latitude 5520"); eq1.setSerialNumber("DL5520-001");
                eq1.setBrand("Dell"); eq1.setCategory("INFORMATIQUE"); eq1.setStatus("AVAILABLE");
                equipmentRepo.save(eq1);
                
                Equipment eq2 = new Equipment(); eq2.setModel("HP EliteBook 840"); eq2.setSerialNumber("HP840-001");
                eq2.setBrand("HP"); eq2.setCategory("INFORMATIQUE"); eq2.setStatus("AVAILABLE");
                equipmentRepo.save(eq2);
                
                Equipment eq3 = new Equipment(); eq3.setModel("iPhone 14"); eq3.setSerialNumber("IP14-001");
                eq3.setBrand("Apple"); eq3.setCategory("TELEPHONIE"); eq3.setStatus("AVAILABLE");
                equipmentRepo.save(eq3);
            }
        };
    }
}