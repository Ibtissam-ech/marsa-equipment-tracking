package com.marsamaroc.equipment.config;

import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner init(UserRepository userRepo, EquipmentRepository equipmentRepo, 
                          CategoryRepository categoryRepo, AssignmentHistoryRepository assignmentRepo,
                          AffectataireRepository affectataireRepo, DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            try {
                jdbc.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            } catch (Exception ignored) {}
            // Always ensure admin user exists with ADMIN role
            if (!userRepo.existsByUsername("amine")) {
                User amine = new User();
                amine.setUsername("amine");
                amine.setPassword("password");
                amine.setFullName("Amine");
                amine.setEmail("amine@marsa.ma");
                amine.setRole("ADMIN");
                amine.setDepartment("Direction");
                amine.setFonction("Administrateur");
                userRepo.save(amine);
            } else {
                User existing = userRepo.findByUsername("amine").get();
                if (!"ADMIN".equals(existing.getRole())) {
                    existing.setRole("ADMIN");
                    userRepo.save(existing);
                }
            }

            // Ensure technician users exist
            String[][] techs = {
                {"technicien1", "Technicien IT 1", "tech1@marsa.ma"},
                {"technicien2", "Technicien IT 2", "tech2@marsa.ma"},
                {"technicien3", "Technicien IT 3", "tech3@marsa.ma"},
            };
            for (String[] t : techs) {
                if (!userRepo.existsByUsername(t[0])) {
                    User tech = new User();
                    tech.setUsername(t[0]);
                    tech.setPassword("password");
                    tech.setFullName(t[1]);
                    tech.setEmail(t[2]);
                    tech.setRole("TECHNICIEN");
                    tech.setDepartment("IT");
                    tech.setFonction("Technicien");
                    userRepo.save(tech);
                }
            }

            // Ensure personnel user exists
            if (!userRepo.existsByUsername("personnel1")) {
                User personnel = new User();
                personnel.setUsername("personnel1");
                personnel.setPassword("password");
                personnel.setFullName("Personnel Test");
                personnel.setEmail("personnel1@marsa.ma");
                personnel.setRole("PERSONNEL");
                personnel.setDepartment("Service");
                personnel.setFonction("Employé");
                userRepo.save(personnel);
            }
            
            // Create affectataires if none exist
            if (affectataireRepo.count() == 0) {
                Affectataire said = new Affectataire();
                said.setUsername("said");
                said.setNom("Oubdi Said");
                said.setEmail("said@marsa.ma");
                said.setDepartment("Maintenance");
                said.setFonction("Technicien");
                affectataireRepo.save(said);
                
                Affectataire soufiane = new Affectataire();
                soufiane.setUsername("soufiane");
                soufiane.setNom("Amenzo Soufiane");
                soufiane.setEmail("soufiane@marsa.ma");
                soufiane.setDepartment("IT");
                soufiane.setFonction("Technicien IT");
                affectataireRepo.save(soufiane);
                
                Affectataire younes = new Affectataire();
                younes.setUsername("younes");
                younes.setNom("Houali Younes");
                younes.setEmail("younes@marsa.ma");
                younes.setDepartment("Maintenance");
                younes.setFonction("Technicien");
                affectataireRepo.save(younes);
                
                Affectataire rida = new Affectataire();
                rida.setUsername("rida");
                rida.setNom("Rami Rida");
                rida.setEmail("rida@marsa.ma");
                rida.setDepartment("IT");
                rida.setFonction("Technicien");
                affectataireRepo.save(rida);
                
                Affectataire amineAffect = new Affectataire();
                amineAffect.setUsername("amine");
                amineAffect.setNom("Deraa Amine");
                amineAffect.setEmail("amine@marsa.ma");
                amineAffect.setDepartment("Direction");
                amineAffect.setFonction("Directeur");
                affectataireRepo.save(amineAffect);
            }
            
            // Always create equipment items if none exist
            if (equipmentRepo.count() == 0) {
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
                
                // Create sample assignments if none exist
                List<Equipment> allEquip = equipmentRepo.findAll();
                List<Affectataire> allAffect = affectataireRepo.findAll();
                if (assignmentRepo.count() == 0 && allEquip.size() >= 5 && allAffect.size() >= 3) {
                    // Assign first 3 affectataires to first 5 equipment items
                    for (int i = 0; i < 5 && i < allEquip.size(); i++) {
                        AssignmentHistory ah = new AssignmentHistory();
                        ah.setEquipment(allEquip.get(i));
                        ah.setAffectataire(allAffect.get(i % allAffect.size()));
                        ah.setStartDate(java.time.LocalDateTime.now());
                        ah.setAssignedBy("SYSTEM");
                        ah.setNotes("Affectation initiale");
                        assignmentRepo.save(ah);
                        
                        allEquip.get(i).setCurrentAffectataire(allAffect.get(i % allAffect.size()));
                        allEquip.get(i).setStatus("ASSIGNED");
                        equipmentRepo.save(allEquip.get(i));
                    }
                }
            }
            
            System.out.println("=========================================");
            System.out.println("Application started!");
            System.out.println("Users: " + userRepo.count() + " | Affectataires: " + affectataireRepo.count() + " | Equipment: " + equipmentRepo.count());
            System.out.println("=========================================");
        };
    }
}