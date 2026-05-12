package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.dto.AssignmentDTO;
import com.marsamaroc.equipment.dto.EquipmentDTO;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import com.marsamaroc.equipment.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EquipmentController {
    private final EquipmentService equipmentService;
    private final UserRepository userRepo;
    private final EquipmentRepository equipmentRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final CategoryRepository categoryRepo;
    private final AffectataireRepository affectataireRepo;
    
    public EquipmentController(EquipmentService s, UserRepository u, EquipmentRepository e, 
                          AssignmentHistoryRepository a, CategoryRepository c,
                          AffectataireRepository af) {
        this.equipmentService = s;
        this.userRepo = u;
        this.equipmentRepo = e;
        this.assignmentRepo = a;
        this.categoryRepo = c;
        this.affectataireRepo = af;
    }
    
    @GetMapping("/equipment")
    public List<EquipmentDTO> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }
    
    @GetMapping("/equipment/{id}")
    public EquipmentDTO getEquipment(@PathVariable Long id) {
        return equipmentService.getEquipment(id);
    }
    
    @PostMapping("/equipment")
    public EquipmentDTO createEquipment(@RequestBody Equipment equipment) {
        return equipmentService.toDTO(equipmentService.saveEquipment(equipment));
    }
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return equipmentService.getAllUsers();
    }
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return equipmentService.getUser(id);
    }
    
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return equipmentService.saveUser(user);
    }
    
    @GetMapping("/affectataires/{id}/assignments")
    public List<AssignmentHistory> getAffectataireAssignments(@PathVariable Long id) {
        return assignmentRepo.findByAffectataireId(id);
    }
    
    @GetMapping("/assignments/current")
    public List<AssignmentDTO> getCurrentAssignments() {
        return equipmentService.getAllAssignmentsAsDTO();
    }
    
    @GetMapping("/assignments/history")
    public List<AssignmentDTO> getAssignmentHistory() {
        return equipmentService.getAllAssignmentsAsDTO();
    }
    
    @GetMapping("/assignments/equipment/{equipmentId}")
    public List<AssignmentHistory> getEquipmentAssignments(@PathVariable Long equipmentId) {
        return assignmentRepo.findByEquipmentIdAndEndDateIsNull(equipmentId);
    }
    
    @PostMapping("/assignments/assign")
    public AssignmentHistory assignEquipment(@RequestBody Map<String, Object> data) {
        Long productId = ((Number) data.get("productId")).longValue();
        Long affectataireId = ((Number) data.get("userId")).longValue();
        Long assignedBy = data.get("assignedBy") != null ? ((Number) data.get("assignedBy")).longValue() : null;
        String notes = (String) data.get("notes");
        String directionOrigine = (String) data.get("directionOrigine");
        String directionDestination = (String) data.get("directionDestination");
        LocalDateTime startDate = null;
        if (data.get("startDate") != null) {
            startDate = LocalDateTime.parse((String) data.get("startDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return equipmentService.assignEquipment(productId, affectataireId, assignedBy, notes, directionOrigine, directionDestination, startDate);
    }
    
    @PostMapping("/assignments/end/{productId}")
    public AssignmentHistory endAssignment(@PathVariable Long productId, @RequestBody Map<String, String> data) {
        return equipmentService.endAssignment(productId, data.get("notes"));
    }
    
    @GetMapping("/categories")
    public List<EquipmentCategory> getAllCategories() {
        return equipmentService.getAllCategories();
    }
    
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        if (equipmentRepo.existsById(id)) {
            equipmentRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/equipment/{id}")
    public EquipmentDTO updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        Equipment existing = equipmentRepo.findById(id).orElse(null);
        if (existing == null) return null;
        if (equipment.getName() != null) existing.setName(equipment.getName());
        if (equipment.getModel() != null) existing.setModel(equipment.getModel());
        if (equipment.getCategory() != null) existing.setCategory(equipment.getCategory());
        if (equipment.getSerialNumber() != null) existing.setSerialNumber(equipment.getSerialNumber());
        if (equipment.getBrand() != null) existing.setBrand(equipment.getBrand());
        if (equipment.getStatus() != null) existing.setStatus(equipment.getStatus());
        return equipmentService.toDTO(equipmentService.saveEquipment(existing));
    }
}