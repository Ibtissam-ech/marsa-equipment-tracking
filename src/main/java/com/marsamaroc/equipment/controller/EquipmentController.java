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
    
    public EquipmentController(EquipmentService s, UserRepository u, EquipmentRepository e, AssignmentHistoryRepository a, TicketRepository t) {
        this.equipmentService = s;
        this.userRepo = u;
        this.equipmentRepo = e;
        this.assignmentRepo = a;
        this.ticketRepo = t;
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
    
    @GetMapping("/assignments/current")
    public List<AssignmentHistory> getCurrentAssignments() {
        return equipmentService.getCurrentAssignments();
    }
    
    @PostMapping("/assignments/assign")
    public AssignmentHistory assignEquipment(@RequestBody Map<String, Object> data) {
        Long assignedById = data.get("assignedBy") != null ? ((Number) data.get("assignedBy")).longValue() : null;
        return equipmentService.assignEquipment(
            ((Number) data.get("productId")).longValue(),
            ((Number) data.get("userId")).longValue(),
            assignedById != null ? "USER-" + assignedById : "SYSTEM",
            (String) data.get("notes")
        );
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
        InterventionTicket t = ticketRepo.findById(id).orElse(null);
        if (t != null) {
            t.setStatus("CLOSED");
            t.setClosedAt(java.time.LocalDateTime.now());
            t.setResolutionNotes((String) data.get("resolutionNotes"));
            return ticketRepo.save(t);
        }
        return null;
    }
}