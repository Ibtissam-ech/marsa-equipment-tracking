package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.service.PdfService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    private final PdfService pdfService;
    
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }
    
    @GetMapping("/fiche-affectation/user/{affectataireId}")
    public ResponseEntity<Resource> generateUserFiche(@PathVariable Long affectataireId) {
        try {
            String fileName = pdfService.generateAffectataireFichePdf(affectataireId);
            if (fileName == null) {
                return ResponseEntity.notFound().build();
            }
            File file = new File(fileName);
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/fiche-affectation/equipment/{equipmentId}")
    public ResponseEntity<Resource> generateEquipmentFiche(@PathVariable Long equipmentId) {
        try {
            String fileName = pdfService.generateEquipmentFichePdf(equipmentId);
            if (fileName == null) {
                return ResponseEntity.notFound().build();
            }
            File file = new File(fileName);
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/fiche-affectation/group")
    public ResponseEntity<Resource> generateGroupFiche(@RequestParam String affectataires) {
        try {
            String[] ids = affectataires.split(",");
            Long[] affIds = new Long[ids.length];
            for (int i = 0; i < ids.length; i++) affIds[i] = Long.parseLong(ids[i].trim());
            String fileName = pdfService.generateGroupFichePdf(affIds);
            if (fileName == null) return ResponseEntity.notFound().build();
            File file = new File(fileName);
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}