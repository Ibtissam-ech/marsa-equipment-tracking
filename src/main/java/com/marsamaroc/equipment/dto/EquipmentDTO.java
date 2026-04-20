package com.marsamaroc.equipment.dto;

import com.marsamaroc.equipment.model.entity.Equipment;

public class EquipmentDTO {
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private String brand;
    private String category;
    private String description;
    private String status;
    private String location;
    private Long createdBy;
    private UserDTO currentUser;
    
    public static class UserDTO {
        private Long id;
        private String fullName;
        private String username;
        
        public UserDTO() {}
        
        public UserDTO(Long id, String fullName, String username) {
            this.id = id;
            this.fullName = fullName;
            this.username = username;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public UserDTO getCurrentUser() { return currentUser; }
    public void setCurrentUser(UserDTO currentUser) { this.currentUser = currentUser; }
    
    public static EquipmentDTO fromEntity(Equipment e) {
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
            UserDTO userDto = new UserDTO();
            userDto.setId(e.getCurrentUser().getId());
            userDto.setFullName(e.getCurrentUser().getFullName());
            userDto.setUsername(e.getCurrentUser().getUsername());
            dto.setCurrentUser(userDto);
        }
        return dto;
    }
}