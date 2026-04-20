package com.marsamaroc.equipment.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;
    
    public PdfService(EquipmentRepository e, UserRepository u, AssignmentHistoryRepository a, TicketRepository t) {
        this.equipmentRepo = e;
        this.userRepo = u;
        this.assignmentRepo = a;
        this.ticketRepo = t;
    }
    
    public String generateFicheAffectation(Long userId) throws Exception {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        
        List<AssignmentHistory> assignments = assignmentRepo.findByUserId(userId);
        List<AssignmentHistory> activeAssignments = assignmentRepo.findByUserIdAndEndDateIsNull(userId);
        
        String outputDir = "./uploads/assignments/";
        new File(outputDir).mkdirs();
        
        String fileName = outputDir + "fiche_affectation_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        Paragraph title = new Paragraph("FICHE D'AFFECTATION")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(18)
            .setBold();
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Marsa Maroc - Equipment Tracking System")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12);
        document.add(subtitle);
        
        document.add(new Paragraph("\n"));
        
        Paragraph userInfo = new Paragraph("UTILISATEUR: " + user.getFullName())
            .setFontSize(12);
        document.add(userInfo);
        
        if (user.getDepartment() != null) {
            document.add(new Paragraph("Département: " + user.getDepartment()).setFontSize(10));
        }
        if (user.getFonction() != null) {
            document.add(new Paragraph("Fonction: " + user.getFonction()).setFontSize(10));
        }
        
        document.add(new Paragraph("\n"));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
            .useAllAvailableWidth();
        
        table.addHeaderCell("Équipement");
        table.addHeaderCell("N° Série");
        table.addHeaderCell("Date d'affectation");
        table.addHeaderCell("Statut");
        
        for (AssignmentHistory a : activeAssignments) {
            if (a.getEquipment() != null) {
                table.addCell(a.getEquipment().getModel() != null ? a.getEquipment().getModel() : "-");
                table.addCell(a.getEquipment().getSerialNumber() != null ? a.getEquipment().getSerialNumber() : "-");
                table.addCell(a.getStartDate() != null ? a.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
                table.addCell("Actif");
            }
        }
        
        document.add(table);
        
        document.add(new Paragraph("\n\n"));
        
        Paragraph footer = new Paragraph("Document généré le " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")))
            .setFontSize(8);
        document.add(footer);
        
        document.close();
        return fileName;
    }
    
    public String generateInterventionPdf(Long ticketId) throws Exception {
        InterventionTicket ticket = ticketRepo.findById(ticketId).orElse(null);
        if (ticket == null) return null;
        
        String outputDir = "./uploads/assignments/";
        new File(outputDir).mkdirs();
        
        String fileName = outputDir + "intervention_" + ticketId + "_" + System.currentTimeMillis() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        Paragraph title = new Paragraph("TICKET D'INTERVENTION")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(18)
            .setBold();
        document.add(title);
        
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Titre: " + ticket.getTitle()).setFontSize(12));
        document.add(new Paragraph("Priorité: " + ticket.getPriority()).setFontSize(10));
        document.add(new Paragraph("Statut: " + ticket.getStatus()).setFontSize(10));
        document.add(new Paragraph("Type: " + ticket.getInterventionType()).setFontSize(10));
        
        if (ticket.getDescription() != null) {
            document.add(new Paragraph("\nDescription: " + ticket.getDescription()).setFontSize(10));
        }
        
        if (ticket.getResolutionNotes() != null) {
            document.add(new Paragraph("\nRésolution: " + ticket.getResolutionNotes()).setFontSize(10));
        }
        
        document.close();
        return fileName;
    }
}