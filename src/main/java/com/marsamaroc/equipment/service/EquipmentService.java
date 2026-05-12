package com.marsamaroc.equipment.service;

import com.marsamaroc.equipment.dto.AssignmentDTO;
import com.marsamaroc.equipment.dto.EquipmentDTO;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final CategoryRepository categoryRepo;
    private final AffectataireRepository affectataireRepo;
    
    public EquipmentService(EquipmentRepository e, UserRepository u, AssignmentHistoryRepository a, CategoryRepository c, AffectataireRepository af) {
        this.equipmentRepo = e;
        this.userRepo = u;
        this.assignmentRepo = a;
        this.categoryRepo = c;
        this.affectataireRepo = af;
    }
    
    public List<EquipmentDTO> getAllEquipment() {
        List<Equipment> all = equipmentRepo.findAll();
        for (Equipment e : all) {
            if (e.getCurrentAffectataire() != null && e.getCurrentAffectataire().getId() != null) {
                Affectataire aff = affectataireRepo.findById(e.getCurrentAffectataire().getId()).orElse(null);
                e.setCurrentAffectataire(aff);
            }
        }
        return all.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    public List<AssignmentDTO> getAllAssignmentsAsDTO() {
        List<AssignmentHistory> all = assignmentRepo.findAll();
        return all.stream().map(a -> {
            AssignmentDTO dto = new AssignmentDTO();
            dto.setId(a.getId());
            if (a.getStartDate() != null) {
                dto.setStartDate(a.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            if (a.getEndDate() != null) {
                dto.setEndDate(a.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            if (a.getEquipment() != null) {
                Equipment eq = equipmentRepo.findById(a.getEquipment().getId()).orElse(null);
                if (eq != null) {
                    dto.setEquipment(new AssignmentDTO.EquipmentSummary(eq.getId(), eq.getModel(), eq.getSerialNumber(), eq.getStatus()));
                }
            }
            
            if (a.getAffectataire() != null) {
                Affectataire aff = affectataireRepo.findById(a.getAffectataire().getId()).orElse(null);
                if (aff != null) {
                    dto.setAffectataire(new AssignmentDTO.AffectataireSummary(aff.getId(), aff.getNom(), null, aff.getDepartment(), aff.getEmail()));
                }
            }
            return dto;
        }).collect(Collectors.toList());
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
    
    public AssignmentHistory assignEquipment(Long equipmentId, Long affectataireId, Long assignedBy, String notes, String directionOrigine, String directionDestination, LocalDateTime startDate) {
        Equipment eq = equipmentRepo.findById(equipmentId).orElse(null);
        Affectataire affectataire = affectataireRepo.findById(affectataireId).orElse(null);
        if (eq == null || affectataire == null) return null;
        
        AssignmentHistory assignment = new AssignmentHistory();
        assignment.setEquipment(eq);
        assignment.setAffectataire(affectataire);
        assignment.setStartDate(startDate != null ? startDate : LocalDateTime.now());
        assignment.setAssignedDate(LocalDateTime.now());
        assignment.setAssignedBy(assignedBy != null ? "USER-" + assignedBy : "SYSTEM");
        assignment.setNotes(notes);
        assignment.setDirectionOrigine(directionOrigine);
        assignment.setDirectionDestination(directionDestination);
        assignmentRepo.save(assignment);
        
        eq.setCurrentAffectataire(affectataire);
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
        
        eq.setCurrentAffectataire(null);
        eq.setStatus("AVAILABLE");
        equipmentRepo.save(eq);
        
        return active.isEmpty() ? null : active.get(0);
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
        if (e.getCurrentAffectataire() != null) {
            dto.setCurrentAffectataire(new EquipmentDTO.AffectataireDTO(
                e.getCurrentAffectataire().getId(),
                e.getCurrentAffectataire().getFullName(),
                e.getCurrentAffectataire().getNom()
            ));
        }
        return dto;
    }
}