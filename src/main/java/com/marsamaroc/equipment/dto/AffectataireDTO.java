package com.marsamaroc.equipment.dto;

public class AffectataireDTO {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String cin;
    private String department;
    private String fonction;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    
    public String getFullName() {
        if (prenom != null && !prenom.isEmpty() && nom != null && !nom.isEmpty()) {
            return prenom + " " + nom;
        } else if (nom != null && !nom.isEmpty()) {
            return nom;
        }
        return username;
    }
}