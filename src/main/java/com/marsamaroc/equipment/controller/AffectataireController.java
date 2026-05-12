package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.dto.AffectataireDTO;
import com.marsamaroc.equipment.model.entity.Affectataire;
import com.marsamaroc.equipment.repository.AffectataireRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AffectataireController {
    private final AffectataireRepository affectataireRepo;
    
    public AffectataireController(AffectataireRepository affectataireRepo) {
        this.affectataireRepo = affectataireRepo;
    }
    
    @GetMapping("/affectataires")
    public List<AffectataireDTO> getAllAffectataires() {
        return affectataireRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @GetMapping("/affectataires/{id}")
    public AffectataireDTO getAffectataire(@PathVariable Long id) {
        return affectataireRepo.findById(id).map(this::toDTO).orElse(null);
    }
    
    @PostMapping("/affectataires")
    public AffectataireDTO createAffectataire(@RequestBody Affectataire a) {
        return toDTO(affectataireRepo.save(a));
    }
    
    @PutMapping("/affectataires/{id}")
    public AffectataireDTO updateAffectataire(@PathVariable Long id, @RequestBody Affectataire data) {
        Affectataire existing = affectataireRepo.findById(id).orElse(null);
        if (existing == null) return null;
        if (data.getNom() != null) existing.setNom(data.getNom());
        if (data.getEmail() != null) existing.setEmail(data.getEmail());
        if (data.getTelephone() != null) existing.setTelephone(data.getTelephone());
        if (data.getCin() != null) existing.setCin(data.getCin());
        if (data.getDepartment() != null) existing.setDepartment(data.getDepartment());
        if (data.getFonction() != null) existing.setFonction(data.getFonction());
        return toDTO(affectataireRepo.save(existing));
    }

    @DeleteMapping("/affectataires/{id}")
    public ResponseEntity<Void> deleteAffectataire(@PathVariable Long id) {
        if (affectataireRepo.existsById(id)) {
            affectataireRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private AffectataireDTO toDTO(Affectataire a) {
        AffectataireDTO dto = new AffectataireDTO();
        dto.setId(a.getId());
        dto.setUsername(a.getUsername());
        dto.setNom(a.getNom());
        dto.setEmail(a.getEmail());
        dto.setTelephone(a.getTelephone());
        dto.setCin(a.getCin());
        dto.setDepartment(a.getDepartment());
        dto.setFonction(a.getFonction());
        return dto;
    }
}