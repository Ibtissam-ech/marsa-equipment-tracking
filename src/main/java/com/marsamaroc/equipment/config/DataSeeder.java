package com.marsamaroc.equipment.config;

import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner init(UserRepository userRepo, EquipmentRepository equipmentRepo, 
                          CategoryRepository categoryRepo, AssignmentHistoryRepository assignmentRepo) {
        return args -> {
            if (userRepo.count() == 0) {
                // Create 5 users
                User said = new User();
                said.setUsername("said");
                said.setPassword("password");
                said.setFullName("Said");
                said.setEmail("said@marsa.ma");
                said.setRole("TECHNICIEN");
                said.setDepartment("Maintenance");
                said.setFonction("Technicien");
                said.setPhoneNumber("+212661234567");
                userRepo.save(said);
                
                User soufiane = new User();
                soufiane.setUsername("soufiane");
                soufiane.setPassword("password");
                soufiane.setFullName("Soufiane");
                soufiane.setEmail("soufiane@marsa.ma");
                soufiane.setRole("TECHNICIEN");
                soufiane.setDepartment("IT");
                soufiane.setFonction("Technicien IT");
                userRepo.save(soufiane);
                
                User younes = new User();
                younes.setUsername("younes");
                younes.setPassword("password");
                younes.setFullName("Younes");
                younes.setEmail("younes@marsa.ma");
                younes.setRole("TECHNICIEN");
                younes.setDepartment("Maintenance");
                younes.setFonction("Technicien");
                userRepo.save(younes);
                
                User rida = new User();
                rida.setUsername("rida");
                rida.setPassword("password");
                rida.setFullName("Rida");
                rida.setEmail("rida@marsa.ma");
                rida.setRole("TECHNICIEN");
                rida.setDepartment("IT");
                rida.setFonction("Technicien");
                userRepo.save(rida);
                
                User amine = new User();
                amine.setUsername("amine");
                amine.setPassword("password");
                amine.setFullName("Amine");
                amine.setEmail("amine@marsa.ma");
                amine.setRole("ADMIN");
                amine.setDepartment("Direction");
                amine.setFonction("Administrateur");
                userRepo.save(amine);
                
                // Create 20 equipment items
                String[][] equipmentData = {
                    {"Dell Latitude 5520", "DL5520-001", "Dell", "INFORMATIQUE"},
                    {"Dell Latitude 5520", "DL5520-002", "Dell", "INFORMATIQUE"},
                    {"HP EliteBook 840", "HP840-001", "HP", "INFORMATIQUE"},
                    {"HP EliteBook 840", "HP840-002", "HP", "INFORMATIQUE"},
                    {"Lenovo ThinkPad X1", "LX1-001", "Lenovo", "INFORMATIQUE"},
                    {"MacBook Pro 14", "MBP14-001", "Apple", "INFORMATIQUE"},
                    {"iPhone 14 Pro", "IP14P-001", "Apple", "TELEPHONIE"},
                    {"iPhone 14", "IP14-001", "Apple", "TELEPHONIE"},
                    {"Samsung Galaxy S23", "SG23-001", "Samsung", "TELEPHONIE"},
                    {"iPad Pro 12.9", "IPP12-001", "Apple", "TELEPHONIE"},
                    {"Dell Monitor 27\"", "DM27-001", "Dell", "INFORMATIQUE"},
                    {"HP LaserJet Pro", "HPLJ-001", "HP", "INFORMATIQUE"},
                    {"Cisco IP Phone", "CIP-001", "Cisco", "TELEPHONIE"},
                    {"Canon Camera EOS", "CEO-001", "Canon", "INFORMATIQUE"},
                    {"Projector Epson", "PE-001", "Epson", "INFORMATIQUE"},
                    {"Scanner Fujitsu", "SF-001", "Fujitsu", "INFORMATIQUE"},
                    {"UPS APC 1500", "UPSA-001", "APC", "INFORMATIQUE"},
                    {"Disk Station Synology", "DSS-001", "Synology", "INFORMATIQUE"},
                    {"Router Cisco", "RC-001", "Cisco", "INFORMATIQUE"},
                    {"Access Point Ubiquiti", "APU-001", "Ubiquiti", "INFORMATIQUE"}
                };
                
                for (String[] data : equipmentData) {
                    Equipment eq = new Equipment();
                    eq.setModel(data[0]);
                    eq.setSerialNumber(data[1]);
                    eq.setBrand(data[2]);
                    eq.setCategory(data[3]);
                    eq.setStatus("AVAILABLE");
                    eq.setLocation("Marsa Maroc HQ");
                    equipmentRepo.save(eq);
                }
                
                System.out.println("=========================================");
                System.out.println("Database seeded with demo data!");
                System.out.println("Users: 5 | Equipment: 20");
                System.out.println("=========================================");
            }
        };
    }
}