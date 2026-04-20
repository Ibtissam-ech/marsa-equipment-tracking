package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {
    List<AssignmentHistory> findByUserIdAndEndDateIsNull(Long userId);
    List<AssignmentHistory> findByEquipmentIdAndEndDateIsNull(Long equipmentId);
    List<AssignmentHistory> findByUserId(Long userId);
}