package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<EquipmentCategory, Long> {
}