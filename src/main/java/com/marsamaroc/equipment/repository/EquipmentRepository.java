package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findByStatus(String status);
}