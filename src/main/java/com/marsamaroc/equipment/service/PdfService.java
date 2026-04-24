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
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;

    private static final DeviceRgb MARSA_BLUE = new DeviceRgb(0x1E, 0x3A, 0x5F);
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(0x77, 0x77, 0x77);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(0xF5, 0xF5, 0xF5);

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

        // Build PDF according to exact format
        doc.add(buildPdfHeader());
        doc.add(buildPdfHRule(MARSA_BLUE, 2f, 4f));
        doc.add(buildPdfMetaInfo(user));
        doc.add(buildPdfEquipmentTable(activeAssignments));
        doc.add(buildPdfSignatureSection(user));
        doc.add(buildPdfFooter());

        doc.close();
        return baos.toByteArray();
    }

    private Table buildPdfHeader() throws Exception {
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

        Paragraph title = new Paragraph("Fiche de charge et affectation")
                .setFontSize(20).setFontColor(MARSA_BLUE);
        Paragraph sub = new Paragraph("matériel informatique")
                .setFontSize(11).setFontColor(ColorConstants.GRAY);

        Cell logoCell;
        if (logo != null) {
            logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        } else {
            logoCell = new Cell().add(new Paragraph("MARSA MAROC").setFontSize(16).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        }

        Cell titleCell = new Cell().add(title).add(sub)
                .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);

        return new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth().setMarginBottom(4)
                .addCell(logoCell).addCell(titleCell);
    }

    private Table buildPdfMetaInfo(User user) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        Paragraph datePara = new Paragraph("Date : " + date).setFontSize(10);
        
        Paragraph userPara = new Paragraph("Affectataire : " + (user.getUsername() != null ? user.getUsername() : "-"))
                .setFontSize(11).setFontColor(MARSA_BLUE);
        
        Paragraph deptPara = new Paragraph("Département : " + (user.getDepartment() != null ? user.getDepartment() : "-")).setFontSize(10);
        
        Paragraph fonctionPara = new Paragraph("Fonction : " + (user.getFonction() != null ? user.getFonction() : "-")).setFontSize(10);

        Cell infoCell = new Cell(1, 3).add(datePara).add(userPara).add(deptPara).add(fonctionPara)
                .setBorder(Border.NO_BORDER);

        return new Table(UnitValue.createPercentArray(new float[]{30, 40, 30}))
                .useAllAvailableWidth().setMarginTop(4).setMarginBottom(6)
                .addCell(infoCell);
    }

    private Table buildPdfEquipmentTable(List<AssignmentHistory> assignments) {
        // 6 columns as requested
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f})).useAllAvailableWidth();

        String[] headers = {
            "Modèle & Marque\nPC Portable",
            "Modèle & Marque\nClavier/Souris",
            "Modèle & Marque\nÉcran",
            "Modèle & Marque\nStation'accueil",
            "Modèle & Marque\nDisque Dur",
            "Affectataire"
        };

        for (String h : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(h).setFontColor(ColorConstants.WHITE).setFontSize(8))
                    .setBackgroundColor(MARSA_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(4);
            table.addHeaderCell(cell);
        }

        if (assignments.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setHeight(40).setBorder(Border.NO_BORDER));
            }
        } else {
            for (AssignmentHistory a : assignments) {
                Equipment e = a.getEquipment();
                String model = e != null ? (e.getName() != null ? e.getName() : e.getModel()) : "-";
                String sn = e != null ? (e.getSerialNumber() != null ? e.getSerialNumber() : "-") : "-";
                
                // First 5 columns show one equipment details
                table.addCell(new Cell().add(new Paragraph(model + "\nSN: " + sn).setFontSize(8)).setBackgroundColor(LIGHT_GRAY).setPadding(3));
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().setBackgroundColor(LIGHT_GRAY).setBorder(Border.NO_BORDER));
                table.addCell(new Cell().add(new Paragraph(a.getUser() != null ? a.getUser().getUsername() : "-").setFontSize(9)).setBackgroundColor(LIGHT_GRAY).setPadding(3));
            }
        }
        return table;
    }

    private Table buildPdfSignatureSection(User user) {
        Cell leftLabel = new Cell().add(new Paragraph("Entité demandeuse").setFontSize(10).setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPaddingBottom(5);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la DSI BUM").setFontSize(10).setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPaddingBottom(5);

        Cell leftBox = new Cell()
                .add(new Paragraph("Nom: " + (user.getUsername() != null ? user.getUsername() : "-")).setFontSize(10))
                .add(new Paragraph("Signature:").setFontSize(10).setMarginTop(25))
                .add(new Paragraph("Cachet:").setFontSize(10).setMarginTop(25))
                .setBorder(new SolidBorder(GRAY_TEXT, 0.5f)).setPadding(10);

        Cell rightBox = new Cell()
                .add(new Paragraph("Nom:").setFontSize(10))
                .add(new Paragraph("Signature:").setFontSize(10).setMarginTop(25))
                .add(new Paragraph("Cachet:").setFontSize(10).setMarginTop(25))
                .setBorder(new SolidBorder(GRAY_TEXT, 0.5f)).setPadding(10);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        table.addCell(leftLabel);
        table.addCell(rightLabel);
        table.addCell(leftBox);
        table.addCell(rightBox);
        return table;
    }

    private Paragraph buildPdfFooter() {
        return new Paragraph("MARSA MAROC")
                .setFontSize(9).setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5);
    }

    private LineSeparator buildPdfHRule(DeviceRgb color, float thickness, float marginBottom) {
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
        new java.io.File(outputDir).mkdirs();
        
        String fileName = outputDir + "fiche_affectation_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        byte[] pdfBytes = generateFicheAffectationPdf(userId);
        
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
        fos.write(pdfBytes);
        fos.close();
        
        return fileName;
    }
    
    public String generateInterventionPdf(Long ticketId) throws Exception {
        InterventionTicket ticket = ticketRepo.findById(ticketId).orElse(null);
        if (ticket == null) return null;
        
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        String fileName = outputDir + "intervention_" + ticketId + "_" + System.currentTimeMillis() + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        document.add(new Paragraph("TICKET D'INTERVENTION")
            .setTextAlignment(TextAlignment.CENTER).setFontSize(18).setBold());
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Titre: " + ticket.getTitle()).setFontSize(12));
        document.add(new Paragraph("Priorité: " + ticket.getPriority()).setFontSize(10));
        document.add(new Paragraph("Statut: " + ticket.getStatus()).setFontSize(10));
        
        if (ticket.getDescription() != null) {
            document.add(new Paragraph("\nDescription: " + ticket.getDescription()).setFontSize(10));
        }
        
        document.close();
        return fileName;
    }
}