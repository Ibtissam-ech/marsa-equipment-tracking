package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findByStatus(String status);
    
    @Query(value = "SELECT e.id, e.name, e.model, e.serial_number, e.brand, e.category, e.status, e.location, af.id, af.nom, af.prenom FROM equipment e LEFT JOIN equipment_assignment_history ah ON ah.equipment_id = e.id AND ah.end_date IS NULL LEFT JOIN affectataires af ON ah.affectataire_id = af.id", nativeQuery = true)
    List<Object[]> findAllWithAffectataireNative();
}