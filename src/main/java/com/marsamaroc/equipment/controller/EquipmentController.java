package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.dto.EquipmentDTO;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import com.marsamaroc.equipment.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EquipmentController {
    private final EquipmentService equipmentService;
    private final UserRepository userRepo;
    private final EquipmentRepository equipmentRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;
    private final CategoryRepository categoryRepo;
    
    public EquipmentController(EquipmentService s, UserRepository u, EquipmentRepository e, 
                          AssignmentHistoryRepository a, TicketRepository t, CategoryRepository c) {
        this.equipmentService = s;
        this.userRepo = u;
        this.equipmentRepo = e;
        this.assignmentRepo = a;
        this.ticketRepo = t;
        this.categoryRepo = c;
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
    
    @GetMapping("/users/{id}/assignments")
    public List<AssignmentHistory> getUserAssignments(@PathVariable Long id) {
        return assignmentRepo.findByUserId(id);
    }
    
    @GetMapping("/assignments/current")
    public List<AssignmentHistory> getCurrentAssignments() {
        return equipmentService.getCurrentAssignments();
    }
    
    @GetMapping("/assignments/history")
    public List<AssignmentHistory> getAssignmentHistory() {
        return assignmentRepo.findAll();
    }
    
    @GetMapping("/assignments/equipment/{equipmentId}")
    public List<AssignmentHistory> getEquipmentAssignments(@PathVariable Long equipmentId) {
        return assignmentRepo.findByEquipmentIdAndEndDateIsNull(equipmentId);
    }
    
    @PostMapping("/assignments/assign")
    public AssignmentHistory assignEquipment(@RequestBody Map<String, Object> data) {
        Long productId = ((Number) data.get("productId")).longValue();
        Long userId = ((Number) data.get("userId")).longValue();
        Long assignedBy = data.get("assignedBy") != null ? ((Number) data.get("assignedBy")).longValue() : null;
        String notes = (String) data.get("notes");
        return equipmentService.assignEquipment(productId, userId, assignedBy, notes);
    }
    
    @PostMapping("/assignments/end/{productId}")
    public AssignmentHistory endAssignment(@PathVariable Long productId, @RequestBody Map<String, String> data) {
        return equipmentService.endAssignment(productId, data.get("notes"));
    }
    
    @GetMapping("/tickets/open")
    public List<InterventionTicket> getOpenTickets() {
        return equipmentService.getOpenTickets();
    }
    
    @PostMapping("/tickets")
    public InterventionTicket createTicket(@RequestBody InterventionTicket ticket) {
        return equipmentService.createTicket(ticket);
    }
    
    @PutMapping("/tickets/{id}/close")
    public InterventionTicket closeTicket(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Long closedBy = data.get("closedBy") != null ? ((Number) data.get("closedBy")).longValue() : null;
        String resolutionNotes = (String) data.get("resolutionNotes");
        return equipmentService.closeTicket(id, closedBy, resolutionNotes);
    }
    
    @GetMapping("/categories")
    public List<EquipmentCategory> getAllCategories() {
        return equipmentService.getAllCategories();
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