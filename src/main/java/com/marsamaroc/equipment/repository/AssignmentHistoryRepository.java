package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.marsamaroc.equipment.model.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {
    List<AssignmentHistory> findByEquipmentIdAndEndDateIsNull(Long equipmentId);
    List<AssignmentHistory> findByAffectataireIdAndEndDateIsNull(Long affectataireId);
    List<AssignmentHistory> findByAffectataireId(Long affectataireId);
    List<AssignmentHistory> findByEndDateIsNull();
    
    @Query(value = "SELECT ah.id, ah.start_date, ah.end_date, " +
                   "e.id, e.model, e.serial_number, e.status, " +
                   "af.id, af.nom, af.prenom " +
                   "FROM equipment_assignment_history ah " +
                   "LEFT JOIN equipment e ON ah.equipment_id = e.id " +
                   "LEFT JOIN affectataires af ON ah.affectataire_id = af.id " +
                   "ORDER BY ah.start_date DESC", nativeQuery = true)
    List<Object[]> findAllRaw();
    
    @Query(value = "SELECT ah.id, ah.start_date, ah.end_date, " +
                   "e.id, e.model, e.serial_number, e.status, " +
                   "af.id, af.nom, af.prenom " +
                   "FROM equipment_assignment_history ah " +
                   "LEFT JOIN equipment e ON ah.equipment_id = e.id " +
                   "LEFT JOIN affectataires af ON ah.affectataire_id = af.id " +
                   "WHERE ah.end_date IS NULL " +
                   "ORDER BY ah.start_date DESC", nativeQuery = true)
    List<Object[]> findAllActiveRaw();
}