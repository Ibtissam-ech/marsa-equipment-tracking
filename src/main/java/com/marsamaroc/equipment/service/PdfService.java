package com.marsamaroc.equipment.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.marsamaroc.equipment.model.entity.AssignmentHistory;
import com.marsamaroc.equipment.model.entity.Equipment;
import com.marsamaroc.equipment.model.entity.InterventionTicket;
import com.marsamaroc.equipment.model.entity.User;
import com.marsamaroc.equipment.repository.AssignmentHistoryRepository;
import com.marsamaroc.equipment.repository.EquipmentRepository;
import com.marsamaroc.equipment.repository.TicketRepository;
import com.marsamaroc.equipment.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {
    private final EquipmentRepository equipmentRepo;
    private final UserRepository userRepo;
    private final AssignmentHistoryRepository assignmentRepo;
    private final TicketRepository ticketRepo;

    private static final DeviceRgb MARSA_BLUE = new DeviceRgb(30, 58, 95);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    public PdfService(EquipmentRepository e, UserRepository u, AssignmentHistoryRepository a, TicketRepository t) {
        this.equipmentRepo = e;
        this.userRepo = u;
        this.assignmentRepo = a;
        this.ticketRepo = t;
    }

    public String generateUserFichePdf(Long userId) throws Exception {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        List<AssignmentHistory> activeAssignments = assignmentRepo.findByUserIdAndEndDateIsNull(userId);
        
        String fileName = "fiche-affectation-" + user.getUsername() + ".pdf";
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        byte[] pdf = buildUserPdf(user, activeAssignments);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputDir + fileName);
        fos.write(pdf);
        fos.close();
        return outputDir + fileName;
    }

    public String generateEquipmentFichePdf(Long equipmentId) throws Exception {
        Equipment equipment = equipmentRepo.findById(equipmentId).orElse(null);
        if (equipment == null) return null;
        List<AssignmentHistory> assignments = assignmentRepo.findByEquipmentIdAndEndDateIsNull(equipmentId);
        
        String userName = !assignments.isEmpty() && assignments.get(0).getUser() != null 
                ? assignments.get(0).getUser().getUsername() : "inconnu";
        String fileName = "fiche-affectation-" + userName + ".pdf";
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        byte[] pdf = buildEquipmentPdf(equipment, assignments);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputDir + fileName);
        fos.write(pdf);
        fos.close();
        return outputDir + fileName;
    }

    private byte[] buildUserPdf(User user, List<AssignmentHistory> assignments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(25, 25, 25, 25);

        doc.add(buildPdfHeader());
        doc.add(buildHRule());
        doc.add(buildDirectionRow(user));
        doc.add(buildHRuleBold());
        doc.add(buildPdfEquipmentTable(assignments));
        doc.add(buildHRuleBold());
        doc.add(buildPdfSignatureSection(user));
        doc.add(buildHRule());
        doc.add(buildPdfFooter());
        
        doc.close();
        return baos.toByteArray();
    }

    private byte[] buildEquipmentPdf(Equipment equipment, List<AssignmentHistory> assignments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(25, 25, 25, 25);

        doc.add(buildPdfHeader());
        doc.add(buildHRule());
        doc.add(buildEquipmentDirectionRow(equipment, assignments));
        doc.add(buildHRuleBold());
        doc.add(buildSingleEquipmentTable(equipment, assignments));
        doc.add(buildHRuleBold());
        doc.add(buildEquipmentSignatureSection(assignments));
        doc.add(buildHRule());
        doc.add(buildPdfFooter());
        
        doc.close();
        return baos.toByteArray();
    }

    private com.itextpdf.layout.element.LineSeparator createHRule() {
        return new com.itextpdf.layout.element.LineSeparator(new SolidLine(0.5f)).setMarginTop(8).setMarginBottom(8);
    }

    private com.itextpdf.layout.element.LineSeparator buildHRule() {
        return new com.itextpdf.layout.element.LineSeparator(new SolidLine(0.5f)).setMarginTop(10).setMarginBottom(10);
    }

    private com.itextpdf.layout.element.LineSeparator buildHRuleBold() {
        return new com.itextpdf.layout.element.LineSeparator(new SolidLine(1.5f)).setMarginTop(12).setMarginBottom(12);
    }

    private Table buildPdfHeader() throws Exception {
        Image logo = null;
        try {
            java.io.InputStream logoStream = getClass().getResourceAsStream("/static/images/logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                if (logoBytes.length > 0) {
                    logo = new Image(ImageDataFactory.create(logoBytes)).setWidth(80).setHeight(35);
                }
            }
        } catch (Exception e) {
        }

        Paragraph title = new Paragraph("Fiche d'affectation")
                .setFontSize(18).setFontColor(MARSA_BLUE).setBold();

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(8);
        if (logo != null) logoCell.add(logo);

        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setPadding(8).setVerticalAlignment(VerticalAlignment.MIDDLE);
        titleCell.add(title);

        return new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth()
                .addCell(logoCell).addCell(titleCell);
    }

    private Table buildDirectionRow(User user) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String direction = user.getDepartment() != null ? user.getDepartment() : "-";

        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);
        dateCell.add(new Paragraph("Date: " + date).setFontSize(10).setBold());

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);
        dirCell.add(new Paragraph("Direction d'origine: " + direction).setFontSize(10));
        dirCell.add(new Paragraph("Direction de destination: " + direction).setFontSize(10));

        row.addCell(dateCell);
        row.addCell(dirCell);

        return row;
    }

    private Table buildEquipmentDirectionRow(Equipment equipment, List<AssignmentHistory> assignments) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String direction = equipment.getCurrentUser() != null && equipment.getCurrentUser().getDepartment() != null 
                ? equipment.getCurrentUser().getDepartment() : "-";

        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);
        dateCell.add(new Paragraph("Date: " + date).setFontSize(10).setBold());

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);
        dirCell.add(new Paragraph("Direction d'origine: " + direction).setFontSize(10));
        dirCell.add(new Paragraph("Direction de destination: " + direction).setFontSize(10));

        row.addCell(dateCell);
        row.addCell(dirCell);

        return row;
    }

    private Table buildPdfEquipmentTable(List<AssignmentHistory> assignments) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f})).useAllAvailableWidth();
        table.setBorder(new SolidBorder(MARSA_BLUE, 1.5f));

        String[] headers = {
            "Modèle & Marque\nPC Portable",
            "Modèle & Marque\nClavier/Souris", 
            "Modèle & Marque\nÉcran",
            "Modèle & Marque\nStation d'accueil",
            "Modèle & Marque\nDisque Dur",
            "Affectataire"
        };

        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFontColor(WHITE).setFontSize(8).setBold())
                    .setBackgroundColor(MARSA_BLUE).setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(8);
            table.addHeaderCell(cell);
        }

        if (assignments.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                table.addCell(new Cell().setHeight(40).setBorder(Border.NO_BORDER));
            }
        } else {
            String[] categories = {"PORTABLE", "KEYBOARD_MOUSE", "MONITOR", "DOCKING", "HDD"};
            Map<String, AssignmentHistory> byCategory = new HashMap<>();
            for (AssignmentHistory a : assignments) {
                String cat = inferCategory(a.getEquipment());
                if (!byCategory.containsKey(cat)) byCategory.put(cat, a);
            }

            for (int col = 0; col < 5; col++) {
                AssignmentHistory ah = byCategory.get(categories[col]);
                String content = ah != null && ah.getEquipment() != null
                        ? ah.getEquipment().getName() + "\n" + ah.getEquipment().getSerialNumber() : "-";
                Cell c = new Cell().add(new Paragraph(content).setFontSize(8)).setPadding(6);
                table.addCell(c);
            }
            
            String affectName = "-";
            if (!assignments.isEmpty() && assignments.get(0).getUser() != null) {
                User u = assignments.get(0).getUser();
                affectName = u.getFullName() != null && !u.getFullName().isEmpty() ? u.getFullName() : u.getUsername();
            }
            Cell last = new Cell().add(new Paragraph(affectName).setFontSize(10)).setPadding(6);
            table.addCell(last);
        }
        return table;
    }

    private Table buildPdfSignatureSection(User user) {
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.5f));

        String userName = user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getUsername();

        Cell leftLabel = new Cell().add(new Paragraph("Entité demandeuse").setFontSize(10).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(8);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la DSI BUM").setFontSize(10).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(8);

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(10);
        leftBox.add(new Paragraph("Nom: " + userName).setFontSize(10));
        leftBox.add(new Paragraph(" ").setFontSize(8));
        leftBox.add(new Paragraph("Signature:").setFontSize(10));
        leftBox.add(new Paragraph(" ").setFontSize(8));
        leftBox.add(new Paragraph("Cachet:").setFontSize(10));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(10);
        rightBox.add(new Paragraph("Nom:").setFontSize(10));
        rightBox.add(new Paragraph(" ").setFontSize(8));
        rightBox.add(new Paragraph("Signature:").setFontSize(10));
        rightBox.add(new Paragraph(" ").setFontSize(8));
        rightBox.add(new Paragraph("Cachet:").setFontSize(10));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        return sigTable;
    }

    private Table buildPdfFooter() {
        return new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth()
                .addCell(new Cell().add(new Paragraph("MARSA MAROC").setFontSize(7).setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER).setPaddingTop(10));
    }

    private Table buildSingleEquipmentTable(Equipment equipment, List<AssignmentHistory> assignments) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65})).useAllAvailableWidth();
        table.setBorder(new SolidBorder(MARSA_BLUE, 1.5f));

        addInfoRow(table, "Catégorie", inferCategory(equipment));
        addInfoRow(table, "Modèle", equipment.getModel() != null ? equipment.getModel() : "-");
        addInfoRow(table, "Marque", equipment.getBrand() != null ? equipment.getBrand() : "-");
        addInfoRow(table, "N° Série", equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "-");

        String affectataire = "-";
        if (!assignments.isEmpty() && assignments.get(0).getUser() != null) {
            affectataire = assignments.get(0).getUser().getUsername();
        }
        addInfoRow(table, "Affectataire", affectataire);

        return table;
    }

    private void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBackgroundColor(LIGHT_GRAY).setPadding(6);
        Cell valueCell = new Cell().add(new Paragraph(value).setFontSize(9)).setPadding(6);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private Table buildEquipmentSignatureSection(List<AssignmentHistory> assignments) {
        String affectataire = "-";
        if (!assignments.isEmpty() && assignments.get(0).getUser() != null) {
            affectataire = assignments.get(0).getUser().getUsername();
        }

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.5f));

        Cell leftLabel = new Cell().add(new Paragraph("Entité demandeuse").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(6);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la DSI BUM").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(6);

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(8);
        leftBox.add(new Paragraph("Nom: " + affectataire).setFontSize(9));
        leftBox.add(new Paragraph(" ").setFontSize(8));
        leftBox.add(new Paragraph("Signature:").setFontSize(9));
        leftBox.add(new Paragraph(" ").setFontSize(8));
        leftBox.add(new Paragraph("Cachet:").setFontSize(9));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(8);
        rightBox.add(new Paragraph("Nom:").setFontSize(9));
        rightBox.add(new Paragraph(" ").setFontSize(8));
        rightBox.add(new Paragraph("Signature:").setFontSize(9));
        rightBox.add(new Paragraph(" ").setFontSize(8));
        rightBox.add(new Paragraph("Cachet:").setFontSize(9));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        return sigTable;
    }

    private String inferCategory(Equipment e) {
        if (e == null) return "PORTABLE";
        String m = (e.getModel() != null ? e.getModel().toLowerCase() : "") + " " + (e.getName() != null ? e.getName().toLowerCase() : "");
        if (m.contains("thinkpad") || m.contains("legion") || m.contains("portable") || m.contains("laptop")) return "PORTABLE";
        else if (m.contains("monitor") || m.contains("écran") || m.contains("vision") || m.contains("27q")) return "MONITOR";
        else if (m.contains("keyboard") || m.contains("mouse") || m.contains("clavier") || m.contains("souris") || m.contains("mk220") || m.contains("combo")) return "KEYBOARD_MOUSE";
        else if (m.contains("dock") || m.contains("station")) return "DOCKING";
        else if (m.contains("ssd") || m.contains("disk") || m.contains("disque") || m.contains("hdd")) return "HDD";
        return "PORTABLE";
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
        document.add(new Paragraph("TICKET D'INTERVENTION").setTextAlignment(TextAlignment.CENTER).setFontSize(18).setBold());
        document.add(new Paragraph("Titre: " + ticket.getTitle()).setFontSize(12));
        document.add(new Paragraph("Priorité: " + ticket.getPriority()).setFontSize(10));
        document.add(new Paragraph("Statut: " + ticket.getStatus()).setFontSize(10));
        if (ticket.getDescription() != null) {
            document.add(new Paragraph("Description: " + ticket.getDescription()).setFontSize(10));
        }
        document.close();
        return fileName;
    }
}