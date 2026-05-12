package com.marsamaroc.equipment.dto;

public class AssignmentDTO {
    private Long id;
    private EquipmentSummary equipment;
    private AffectataireSummary affectataire;
    private String startDate;
    private String endDate;
    private String notes;

    public static class EquipmentSummary {
        private Long id;
        private String model;
        private String serialNumber;
        private String status;

        public EquipmentSummary(Long id, String model, String serialNumber, String status) {
            this.id = id; this.model = model; this.serialNumber = serialNumber; this.status = status;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class AffectataireSummary {
        private Long id;
        private String nom;
        private String prenom;
        private String fullName;
        private String department;
        private String email;

        public AffectataireSummary(Long id, String nom, String prenom, String department, String email) {
            this.id = id; this.nom = nom; this.prenom = prenom;
            this.department = department; this.email = email;
            this.fullName = nom != null ? nom : prenom;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EquipmentSummary getEquipment() { return equipment; }
    public void setEquipment(EquipmentSummary equipment) { this.equipment = equipment; }
    public AffectataireSummary getAffectataire() { return affectataire; }
    public void setAffectataire(AffectataireSummary affectataire) { this.affectataire = affectataire; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
