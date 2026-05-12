package com.marsamaroc.equipment.model.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_assignment_history")
public class AssignmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "affectataire_id")
    private Affectataire affectataire;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    private String notes;
    private String assignedBy;

    @Column(name = "direction_origine")
    private String directionOrigine;

    @Column(name = "direction_destination")
    private String directionDestination;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public Affectataire getAffectataire() { return affectataire; }
    public void setAffectataire(Affectataire affectataire) { this.affectataire = affectataire; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public String getDirectionOrigine() { return directionOrigine; }
    public void setDirectionOrigine(String directionOrigine) { this.directionOrigine = directionOrigine; }
    public String getDirectionDestination() { return directionDestination; }
    public void setDirectionDestination(String directionDestination) { this.directionDestination = directionDestination; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }
}