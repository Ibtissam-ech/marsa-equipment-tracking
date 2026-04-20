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
    
    @GetMapping("/fiche-affectation/{userId}")
    public ResponseEntity<Resource> generateFicheAffectation(@PathVariable Long userId) {
        try {
            String fileName = pdfService.generateFicheAffectation(userId);
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
    
    @GetMapping("/intervention/{ticketId}")
    public ResponseEntity<Resource> generateInterventionPdf(@PathVariable Long ticketId) {
        try {
            String fileName = pdfService.generateInterventionPdf(ticketId);
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
}