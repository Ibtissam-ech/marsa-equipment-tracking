package com.marsamaroc.equipment.service;

import com.marsamaroc.equipment.dto.EquipmentDTO;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;
    private final CategoryRepository categoryRepo;
    
    public EquipmentService(EquipmentRepository e, UserRepository u, AssignmentHistoryRepository a, TicketRepository t, CategoryRepository c) {
        this.equipmentRepo = e;
        this.userRepo = u;
        this.assignmentRepo = a;
        this.ticketRepo = t;
        this.categoryRepo = c;
    }
    
    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    public EquipmentDTO getEquipment(Long id) {
        return equipmentRepo.findById(id).map(this::toDTO).orElse(null);
    }
    
    public Equipment saveEquipment(Equipment e) {
        return equipmentRepo.save(e);
    }
    
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
    
    public User getUser(Long id) {
        return userRepo.findById(id).orElse(null);
    }
    
    public User saveUser(User u) {
        return userRepo.save(u);
    }
    
    public List<AssignmentHistory> getCurrentAssignments() {
        return assignmentRepo.findAll().stream()
            .filter(a -> a.getEndDate() == null)
            .collect(Collectors.toList());
    }
    
    public AssignmentHistory assignEquipment(Long equipmentId, Long userId, Long assignedBy, String notes) {
        Equipment eq = equipmentRepo.findById(equipmentId).orElse(null);
        User user = userRepo.findById(userId).orElse(null);
        if (eq == null || user == null) return null;
        
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setEquipment(eq);
        assignment.setUser(user);
        assignment.setStartDate(LocalDateTime.now());
        assignment.setAssignedBy(assignedBy != null ? "USER-" + assignedBy : "SYSTEM");
        assignment.setNotes(notes);
        assignmentRepo.save(assignment);
        
        eq.setCurrentUser(user);
        eq.setStatus("ASSIGNED");
        equipmentRepo.save(eq);
        
        return assignment;
    }
    
    public AssignmentHistory endAssignment(Long equipmentId, String notes) {
        Equipment eq = equipmentRepo.findById(equipmentId).orElse(null);
        if (eq == null) return null;
        
        List<AssignmentHistory> active = assignmentRepo.findByEquipmentIdAndEndDateIsNull(equipmentId);
        for (AssignmentHistory a : active) {
            a.setEndDate(LocalDateTime.now());
            assignmentRepo.save(a);
        }
        
        eq.setCurrentUser(null);
        eq.setStatus("AVAILABLE");
        equipmentRepo.save(eq);
        
        return active.isEmpty() ? null : active.get(0);
    }
    
    public List<InterventionTicket> getOpenTickets() {
        return ticketRepo.findByStatus("OPEN");
    }
    
    public InterventionTicket createTicket(InterventionTicket t) {
        return ticketRepo.save(t);
    }
    
    public InterventionTicket closeTicket(Long ticketId, Long closedBy, String resolutionNotes) {
        InterventionTicket t = ticketRepo.findById(ticketId).orElse(null);
        if (t != null) {
            t.setStatus("CLOSED");
            t.setClosedAt(LocalDateTime.now());
            t.setClosedBy(closedBy);
            t.setResolutionNotes(resolutionNotes);
            return ticketRepo.save(t);
        }
        return null;
    }
    
    public List<EquipmentCategory> getAllCategories() {
        return categoryRepo.findAll();
    }
    
    public EquipmentDTO toDTO(Equipment e) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setModel(e.getModel());
        dto.setSerialNumber(e.getSerialNumber());
        dto.setBrand(e.getBrand());
        dto.setCategory(e.getCategory());
        dto.setDescription(e.getDescription());
        dto.setStatus(e.getStatus());
        dto.setLocation(e.getLocation());
        dto.setCreatedBy(e.getCreatedBy());
        if (e.getCurrentUser() != null) {
            dto.setCurrentUser(new EquipmentDTO.UserDTO(
                e.getCurrentUser().getId(),
                e.getCurrentUser().getFullName(),
                e.getCurrentUser().getUsername()
            ));
        }
        return dto;
    }
}