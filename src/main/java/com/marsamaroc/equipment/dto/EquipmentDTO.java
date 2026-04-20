package com.marsamaroc.equipment.dto;

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
    
    // Getters and Setters
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
    public UserDTO getCurrentUser() { return currentUser; }
    public void setCurrentUser(UserDTO currentUser) { this.currentUser = currentUser; }
}