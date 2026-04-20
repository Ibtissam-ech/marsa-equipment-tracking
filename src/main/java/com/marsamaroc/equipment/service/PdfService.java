package com.marsamaroc.equipment.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.marsamaroc.equipment.model.entity.*;
import com.marsamaroc.equipment.repository.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;

    private static final DeviceRgb MARSA_BLUE = new DeviceRgb(0x1E, 0x3A, 0x5F);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(0xF5, 0xF5, 0xF5);
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(0x77, 0x77, 0x77);

    public PdfService(EquipmentRepository e, UserRepository u, AssignmentHistoryRepository a, TicketRepository t) {
        this.equipmentRepo = e;
        this.userRepo = u;
        this.assignmentRepo = a;
        this.ticketRepo = t;
    }

    public byte[] generateFicheAffectationPdf(Long userId) throws Exception {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;

        List<AssignmentHistory> activeAssignments = assignmentRepo.findByUserIdAndEndDateIsNull(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(20, 18, 15, 18);

        doc.add(buildHeader());
        doc.add(buildHRule(MARSA_BLUE, 2f, 4f));
        doc.add(buildUserInfo(user));
        doc.add(buildEquipmentTable(activeAssignments));
        doc.add(buildSignatureSection(user));
        doc.add(buildFooter());

        doc.close();
        return baos.toByteArray();
    }

    private Table buildHeader() throws Exception {
        // Try to load logo, but continue without it if it fails
        Image logo = null;
        try {
            InputStream logoStream = getClass().getResourceAsStream("/static/logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                if (logoBytes.length > 0) {
                    logo = new Image(ImageDataFactory.create(logoBytes)).setWidth(80).setHeight(35);
                }
            }
        } catch (Exception e) {
            // Continue without logo
        }
        
        Paragraph title = new Paragraph("Fiche d'affectation")
                .setFontSize(18).setFontColor(MARSA_BLUE);
        Paragraph sub = new Paragraph("Matériel informatique")
                .setFontSize(10).setFontColor(ColorConstants.GRAY);

        Cell logoCell;
        if (logo != null) {
            logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            logoCell = new Cell().add(new Paragraph("MARSA MAROC").setFontSize(14).setBold())
                .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        }

        Cell titleCell = new Cell()
                .add(title).add(sub)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        return new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(4)
                .addCell(logoCell)
                .addCell(titleCell);
    }

    private Table buildUserInfo(User user) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        Paragraph datePara = new Paragraph("Date : " + date).setFontSize(10);
        Paragraph namePara = new Paragraph("Affectataire : " + (user.getFullName() != null ? user.getFullName() : user.getUsername()))
                .setFontSize(11).setFontColor(MARSA_BLUE);
        Paragraph deptPara = new Paragraph("Département : " + (user.getDepartment() != null ? user.getDepartment() : "-")).setFontSize(10);
        Paragraph fonctionPara = new Paragraph("Fonction : " + (user.getFonction() != null ? user.getFonction() : "-")).setFontSize(10);

        Cell leftCell = new Cell().add(datePara).setBorder(Border.NO_BORDER);
        Cell rightCell = new Cell().add(namePara).add(deptPara).add(fonctionPara).setBorder(Border.NO_BORDER);

        return new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth().setMarginTop(4).setMarginBottom(6)
                .addCell(leftCell).addCell(rightCell);
    }

    private Table buildEquipmentTable(List<AssignmentHistory> assignments) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 1})).useAllAvailableWidth();

        String[] headers = {"Équipement", "Modèle", "N° Série", "Date affectation", "Statut"};
        for (String h : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(h).setFontColor(ColorConstants.WHITE).setFontSize(9))
                    .setBackgroundColor(MARSA_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(5);
            table.addHeaderCell(cell);
        }

        if (assignments.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setHeight(30).setBorder(Border.NO_BORDER));
            }
        } else {
            for (AssignmentHistory a : assignments) {
                Equipment e = a.getEquipment();
                table.addCell(new Cell().add(new Paragraph(e != null ? e.getName() : "-").setFontSize(9)).setBackgroundColor(LIGHT_GRAY).setPadding(4));
                table.addCell(new Cell().add(new Paragraph(e != null && e.getModel() != null ? e.getModel() : "-").setFontSize(9)).setBackgroundColor(LIGHT_GRAY).setPadding(4));
                table.addCell(new Cell().add(new Paragraph(e != null && e.getSerialNumber() != null ? e.getSerialNumber() : "-").setFontSize(9)).setBackgroundColor(LIGHT_GRAY).setPadding(4));
                table.addCell(new Cell().add(new Paragraph(a.getStartDate() != null ? a.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-").setFontSize(9)).setBackgroundColor(LIGHT_GRAY).setPadding(4));
                table.addCell(new Cell().add(new Paragraph("Actif").setFontSize(9)).setBackgroundColor(new DeviceRgb(0x10, 0xB9, 0x81)).setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER).setPadding(4));
            }
        }
        return table;
    }

    private Table buildSignatureSection(User user) {
        Cell leftLabel = new Cell().add(new Paragraph("Entité demandeuse").setFontSize(9).setFontColor(MARSA_BLUE)).setBorder(Border.NO_BORDER).setPaddingBottom(3);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la DSI BUM").setFontSize(9).setFontColor(MARSA_BLUE)).setBorder(Border.NO_BORDER).setPaddingBottom(3);

        Cell leftBox = new Cell()
                .add(new Paragraph("Nom: " + (user.getFullName() != null ? user.getFullName() : user.getUsername())).setFontSize(9))
                .add(new Paragraph("Signature:").setFontSize(9).setMarginTop(20))
                .add(new Paragraph("Cachet:").setFontSize(9).setMarginTop(20))
                .setBorder(new SolidBorder(GRAY_TEXT, 0.5f)).setPadding(8);

        Cell rightBox = new Cell()
                .add(new Paragraph("Nom:").setFontSize(9))
                .add(new Paragraph("Signature:").setFontSize(9).setMarginTop(20))
                .add(new Paragraph("Cachet:").setFontSize(9).setMarginTop(20))
                .setBorder(new SolidBorder(GRAY_TEXT, 0.5f)).setPadding(8);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        table.addCell(leftLabel);
        table.addCell(rightLabel);
        table.addCell(leftBox);
        table.addCell(rightBox);
        return table;
    }

    private Paragraph buildFooter() {
        return new Paragraph("MARSA MAROC - Document généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(8).setFontColor(GRAY_TEXT).setTextAlignment(TextAlignment.CENTER).setMarginTop(3);
    }

    private LineSeparator buildHRule(DeviceRgb color, float thickness, float marginBottom) {
        LineSeparator line = new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(thickness));
        line.setStrokeColor(color);
        line.setMarginBottom(marginBottom);
        return line;
    }

    public String generateFicheAffectation(Long userId) throws Exception {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        
        List<AssignmentHistory> activeAssignments = assignmentRepo.findByUserIdAndEndDateIsNull(userId);
        
        String outputDir = "./uploads/assignments/";
        new File(outputDir).mkdirs();
        
        String fileName = outputDir + "fiche_affectation_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        byte[] pdfBytes = generateFicheAffectationPdf(userId);
        
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(pdfBytes);
        fos.close();
        
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