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
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.marsamaroc.equipment.model.entity.AssignmentHistory;
import com.marsamaroc.equipment.model.entity.Affectataire;
import com.marsamaroc.equipment.model.entity.Equipment;
import com.marsamaroc.equipment.model.entity.User;
import com.marsamaroc.equipment.repository.AssignmentHistoryRepository;
import com.marsamaroc.equipment.repository.AffectataireRepository;
import com.marsamaroc.equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {
    private final EquipmentRepository equipmentRepo;
    private final AffectataireRepository affectataireRepo;
    private final AssignmentHistoryRepository assignmentRepo;

    private static final DeviceRgb MARSA_BLUE = new DeviceRgb(14, 55, 107);
    private static final DeviceRgb MARSA_BLUE_LIGHT = new DeviceRgb(40, 90, 155);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb OFF_WHITE = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(236, 242, 250);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(30, 35, 45);
    private static final DeviceRgb TEXT_MID = new DeviceRgb(80, 90, 105);
    private static final DeviceRgb BORDER_SOFT = new DeviceRgb(210, 218, 230);

    private static final String[] CATEGORY_ORDER = {"PC_FIXE", "PORTABLE", "KEYBOARD_MOUSE", "MONITOR", "DOCKING", "ADAPTER", "HDD"};
    private static final String[] CATEGORY_HEADERS = {
        "Mod\u00E8le & Marque\nPC fixe",
        "Mod\u00E8le & Marque\nPc Portable",
        "Mod\u00E8le & Marque\nClavier et souris",
        "Mod\u00E8le & Marque\n\u00C9cran",
        "Mod\u00E8le & Marque\nStation d'accueil",
        "Mod\u00E8le & Marque\nAdaptateur",
        "Mod\u00E8le & Marque\nDisque dur"
    };

    public PdfService(EquipmentRepository e, AffectataireRepository af, AssignmentHistoryRepository a) {
        this.equipmentRepo = e;
        this.affectataireRepo = af;
        this.assignmentRepo = a;
    }
    
    public String generateAffectataireFichePdf(Long affectataireId) throws Exception {
        Affectataire affectataire = affectataireRepo.findById(affectataireId).orElse(null);
        if (affectataire == null) return null;
        List<AssignmentHistory> activeAssignments = assignmentRepo.findByAffectataireIdAndEndDateIsNull(affectataireId);
        
        String fileName = "fiche-affectation-" + affectataire.getFullName() + ".pdf";
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        byte[] pdf = buildAffectatairePdf(affectataire, activeAssignments);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputDir + fileName);
        fos.write(pdf);
        fos.close();
        return outputDir + fileName;
    }

    public String generateEquipmentFichePdf(Long equipmentId) throws Exception {
        Equipment equipment = equipmentRepo.findById(equipmentId).orElse(null);
        if (equipment == null) return null;
        List<AssignmentHistory> assignments = assignmentRepo.findByEquipmentIdAndEndDateIsNull(equipmentId);
        
        String userName = !assignments.isEmpty() && assignments.get(0).getAffectataire() != null 
                ? assignments.get(0).getAffectataire().getFullName() : "inconnu";
        String fileName = "fiche-affectation-" + userName + ".pdf";
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        byte[] pdf = buildEquipmentPdf(equipment, assignments);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputDir + fileName);
        fos.write(pdf);
        fos.close();
        return outputDir + fileName;
    }

    public String generateGroupFichePdf(Long[] affectataireIds) throws Exception {
        StringBuilder sb = new StringBuilder("fiche-groupe-");
        for (int i = 0; i < affectataireIds.length; i++) {
            if (i > 0) sb.append("-");
            sb.append(affectataireIds[i]);
        }
        String fileName = sb.append(".pdf").toString();
        String outputDir = "./uploads/assignments/";
        new java.io.File(outputDir).mkdirs();
        
        byte[] pdf = buildGroupPdf(affectataireIds);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(outputDir + fileName);
        fos.write(pdf);
        fos.close();
        return outputDir + fileName;
    }

    private byte[] buildGroupPdf(Long[] affectataireIds) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(25, 25, 25, 25);

        doc.add(buildPdfHeader());
        doc.add(buildHRule());

        Map<String, String> firstDirections = new LinkedHashMap<>();
        for (Long aid : affectataireIds) {
            Affectataire aff = affectataireRepo.findById(aid).orElse(null);
            List<AssignmentHistory> assignments = assignmentRepo.findByAffectataireIdAndEndDateIsNull(aid);
            if (aff != null && !assignments.isEmpty()) {
                AssignmentHistory ah = assignments.get(0);
                String dirOrig = (ah.getDirectionOrigine() != null && !ah.getDirectionOrigine().isEmpty()) ? ah.getDirectionOrigine() : (aff.getDepartment() != null ? aff.getDepartment() : "-");
                String dirDest = (ah.getDirectionDestination() != null && !ah.getDirectionDestination().isEmpty()) ? ah.getDirectionDestination() : "BU Maintenance";
                firstDirections.put(dirOrig + "||" + dirDest, aff.getFullName());
            }
        }
        if (!firstDirections.isEmpty()) {
            String[] dirs = firstDirections.keySet().iterator().next().split("\\|\\|");
            doc.add(buildGroupDirectionRow(dirs[0], dirs[1]));
            doc.add(buildHRuleBold());
        }

        List<GroupRowData> groupRows = new ArrayList<>();
        for (Long aid : affectataireIds) {
            Affectataire aff = affectataireRepo.findById(aid).orElse(null);
            if (aff == null) continue;
            List<AssignmentHistory> assignments = assignmentRepo.findByAffectataireIdAndEndDateIsNull(aid);
            Map<String, AssignmentHistory> byCategory = groupByCategory(assignments);
            groupRows.add(new GroupRowData(aff, byCategory));
        }

        doc.add(buildGroupEquipmentTable(groupRows));
        doc.add(buildHRuleBold());
        doc.add(buildGroupSignatureSection(groupRows));
        doc.add(buildHRule());
        doc.add(buildPdfFooter());
        
        doc.close();
        return baos.toByteArray();
    }

    private Table buildGroupDirectionRow(String dirOrig, String dirDest) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        row.setMarginTop(4).setMarginBottom(4);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        Paragraph dateLabel = new Paragraph("Date: ").setFontSize(10).setFontColor(TEXT_MID);
        Paragraph dateValue = new Paragraph(date).setFontSize(10).setFontColor(TEXT_DARK).setBold();
        dateCell.add(dateLabel).add(dateValue);

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        dirCell.add(new Paragraph("Direction d'origine: " + dirOrig).setFontSize(10).setFontColor(TEXT_DARK));
        dirCell.add(new Paragraph("Direction de destination: " + dirDest).setFontSize(10).setFontColor(TEXT_DARK));

        row.addCell(dateCell).addCell(dirCell);
        return row;
    }

    private Table buildGroupEquipmentTable(List<GroupRowData> rows) {
        float[] widths = {1.5f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.3f};
        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        table.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        String[] headers = {
            "PC Portable",
            "Clavier/Souris",
            "\u00C9cran",
            "Station d'accueil",
            "Adaptateur",
            "Disque dur",
            "Affectataire"
        };

        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFontColor(WHITE).setFontSize(7).setBold())
                    .setBackgroundColor(MARSA_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(4).setPaddingRight(4)
                    .setBorder(new SolidBorder(MARSA_BLUE, 0.5f));
            table.addHeaderCell(cell);
        }

        String[] cats = {"PORTABLE", "KEYBOARD_MOUSE", "MONITOR", "DOCKING", "ADAPTER", "HDD"};

        for (int i = 0; i < rows.size(); i++) {
            GroupRowData row = rows.get(i);
            for (int c = 0; c < cats.length; c++) {
                AssignmentHistory ah = row.byCategory.get(cats[c]);
                String content = "-";
                if (ah != null && ah.getEquipment() != null) {
                    Equipment eq = ah.getEquipment();
                    content = eq.getName() != null ? eq.getName() : "-";
                    if (eq.getSerialNumber() != null && !eq.getSerialNumber().isEmpty()) {
                        content += "\n(SN: " + eq.getSerialNumber() + ")";
                    }
                }
                Cell cell = new Cell().add(new Paragraph(content).setFontSize(7).setFontColor(TEXT_DARK))
                        .setPadding(6).setTextAlignment(TextAlignment.CENTER)
                        .setBorder(new SolidBorder(BORDER_SOFT, 0.5f))
                        .setBackgroundColor(i % 2 == 0 ? OFF_WHITE : WHITE);
                table.addCell(cell);
            }
            Cell nameCell = new Cell().add(new Paragraph(row.affectataire.getFullName()).setFontSize(8).setFontColor(MARSA_BLUE).setBold())
                    .setPadding(6).setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(BORDER_SOFT, 0.5f))
                    .setBackgroundColor(i % 2 == 0 ? OFF_WHITE : WHITE);
            table.addCell(nameCell);
        }

        return table;
    }

    private Table buildGroupSignatureSection(List<GroupRowData> rows) {
        Table wrapper = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        wrapper.setMarginTop(8);

        Paragraph sectionTitle = new Paragraph("SIGNATURES & VALIDATION")
                .setFontSize(8).setFontColor(MARSA_BLUE_LIGHT).setBold().setMarginBottom(10);

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        Cell leftLabel = new Cell().add(new Paragraph("Entit\u00E9 demandeuse").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la SDBUM").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);

        StringBuilder names = new StringBuilder();
        for (GroupRowData r : rows) {
            if (names.length() > 0) names.append(", ");
            names.append(r.affectataire.getFullName());
        }

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        leftBox.add(new Paragraph("Noms: " + names.toString()).setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        rightBox.add(new Paragraph("Nom:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        wrapper.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).add(sectionTitle));
        wrapper.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).add(sigTable));
        return wrapper;
    }

    private static class GroupRowData {
        Affectataire affectataire;
        Map<String, AssignmentHistory> byCategory;
        GroupRowData(Affectataire a, Map<String, AssignmentHistory> bc) {
            this.affectataire = a;
            this.byCategory = bc;
        }
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
    
    private byte[] buildAffectatairePdf(Affectataire affectataire, List<AssignmentHistory> assignments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(25, 25, 25, 25);

        doc.add(buildPdfHeader());
        doc.add(buildHRule());
        doc.add(buildAffectataireDirectionRow(affectataire, assignments));
        doc.add(buildHRuleBold());
        doc.add(buildPdfEquipmentTable(assignments));
        doc.add(buildHRuleBold());
        doc.add(buildAffectataireSignatureSection(affectataire));
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

    private com.itextpdf.layout.element.LineSeparator buildHRule() {
        return new com.itextpdf.layout.element.LineSeparator(new SolidLine(0.8f)).setMarginTop(12).setMarginBottom(12);
    }

    private com.itextpdf.layout.element.LineSeparator buildHRuleBold() {
        return new com.itextpdf.layout.element.LineSeparator(new SolidLine(1.2f)).setMarginTop(14).setMarginBottom(14);
    }

    private Table buildPdfHeader() throws Exception {
        Image logo = null;
        try {
            java.io.InputStream logoStream = getClass().getResourceAsStream("/static/images/logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                if (logoBytes.length > 0) {
                    logo = new Image(ImageDataFactory.create(logoBytes)).setWidth(90).setHeight(40);
                }
            }
        } catch (Exception e) {
        }

        Div headerBar = new Div().setBackgroundColor(WHITE).setPaddingTop(14).setPaddingBottom(14).setPaddingLeft(16).setPaddingRight(16);

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (logo != null) logoCell.add(logo);

        Paragraph title = new Paragraph("Fiche d\u00E9charge et affectation")
                .setFontSize(18).setFontColor(MARSA_BLUE).setBold();
        Paragraph subtitle = new Paragraph("mat\u00E9riel informatique")
                .setFontSize(12).setFontColor(TEXT_MID).setMarginTop(2);

        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setPaddingLeft(8).setVerticalAlignment(VerticalAlignment.MIDDLE);
        titleCell.add(title).add(subtitle);

        headerTable.addCell(logoCell).addCell(titleCell);

        headerBar.add(headerTable);

        Table wrapper = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
        wrapper.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).add(headerBar));
        return wrapper;
    }

    private Table buildDirectionRow(User user) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String direction = user.getDepartment() != null ? user.getDepartment() : "-";

        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        row.setMarginTop(4).setMarginBottom(4);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        Paragraph dateLabel = new Paragraph("Date: ").setFontSize(10).setFontColor(TEXT_MID);
        Paragraph dateValue = new Paragraph(date).setFontSize(10).setFontColor(TEXT_DARK).setBold();
        dateCell.add(dateLabel).add(dateValue);

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        dirCell.add(new Paragraph("Direction d'origine: " + direction).setFontSize(10).setFontColor(TEXT_DARK));
        dirCell.add(new Paragraph("Direction de destination: " + direction).setFontSize(10).setFontColor(TEXT_DARK));

        row.addCell(dateCell).addCell(dirCell);
        return row;
    }
    
    private Table buildAffectataireDirectionRow(Affectataire affectataire, List<AssignmentHistory> assignments) {
        String date = "Date inconnue";
        String dirOrig = "";
        String dirDest = "";

        if (!assignments.isEmpty()) {
            AssignmentHistory ah = assignments.get(0);
            if (ah.getStartDate() != null) {
                date = ah.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (ah.getDirectionOrigine() != null && !ah.getDirectionOrigine().isEmpty()) {
                dirOrig = ah.getDirectionOrigine();
            }
            if (ah.getDirectionDestination() != null && !ah.getDirectionDestination().isEmpty()) {
                dirDest = ah.getDirectionDestination();
            }
        }

        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        row.setMarginTop(4).setMarginBottom(4);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        Paragraph dateLabel = new Paragraph("Date: ").setFontSize(10).setFontColor(TEXT_MID);
        Paragraph dateValue = new Paragraph(date).setFontSize(10).setFontColor(TEXT_DARK).setBold();
        dateCell.add(dateLabel).add(dateValue);

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        dirCell.add(new Paragraph("Direction d'origine: " + dirOrig).setFontSize(10).setFontColor(TEXT_DARK));
        dirCell.add(new Paragraph("Direction de destination: " + dirDest).setFontSize(10).setFontColor(TEXT_DARK));

        row.addCell(dateCell).addCell(dirCell);
        return row;
    }

    private Table buildEquipmentDirectionRow(Equipment equipment, List<AssignmentHistory> assignments) {
        String date = "Date inconnue";
        String dirOrig = "";
        String dirDest = "";

        if (!assignments.isEmpty()) {
            AssignmentHistory ah = assignments.get(0);
            if (ah.getStartDate() != null) {
                date = ah.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (ah.getDirectionOrigine() != null && !ah.getDirectionOrigine().isEmpty()) {
                dirOrig = ah.getDirectionOrigine();
            }
            if (ah.getDirectionDestination() != null && !ah.getDirectionDestination().isEmpty()) {
                dirDest = ah.getDirectionDestination();
            }
        }

        Table row = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        row.setMarginTop(4).setMarginBottom(4);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        Paragraph dateLabel = new Paragraph("Date: ").setFontSize(10).setFontColor(TEXT_MID);
        Paragraph dateValue = new Paragraph(date).setFontSize(10).setFontColor(TEXT_DARK).setBold();
        dateCell.add(dateLabel).add(dateValue);

        Cell dirCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        dirCell.add(new Paragraph("Direction d'origine: " + dirOrig).setFontSize(10).setFontColor(TEXT_DARK));
        dirCell.add(new Paragraph("Direction de destination: " + dirDest).setFontSize(10).setFontColor(TEXT_DARK));

        row.addCell(dateCell).addCell(dirCell);
        return row;
    }

    private Map<String, AssignmentHistory> groupByCategory(List<AssignmentHistory> assignments) {
        Map<String, AssignmentHistory> byCategory = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) {
            for (AssignmentHistory a : assignments) {
                String inferred = inferCategory(a.getEquipment());
                if (inferred.equals(cat) && !byCategory.containsKey(cat)) {
                    byCategory.put(cat, a);
                    break;
                }
            }
        }
        return byCategory;
    }

    private float[] buildColumnWidths(int equipmentColumnCount) {
        float[] widths = new float[equipmentColumnCount + 1];
        for (int i = 0; i < equipmentColumnCount; i++) {
            widths[i] = 1f;
        }
        widths[equipmentColumnCount] = 1.2f;
        return widths;
    }

    private Table buildPdfEquipmentTable(List<AssignmentHistory> assignments) {
        Map<String, AssignmentHistory> byCategory = groupByCategory(assignments);
        List<String> presentCategories = new ArrayList<>(byCategory.keySet());
        int colCount = presentCategories.size();

        float[] widths = buildColumnWidths(colCount);
        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        table.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        for (String cat : presentCategories) {
            int idx = -1;
            for (int i = 0; i < CATEGORY_ORDER.length; i++) {
                if (CATEGORY_ORDER[i].equals(cat)) { idx = i; break; }
            }
            String header = (idx >= 0) ? CATEGORY_HEADERS[idx] : cat;
            Cell cell = new Cell().add(new Paragraph(header).setFontColor(WHITE).setFontSize(8).setBold())
                    .setBackgroundColor(MARSA_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(5).setPaddingRight(5)
                    .setBorder(new SolidBorder(MARSA_BLUE, 0.5f));
            table.addHeaderCell(cell);
        }

        Cell affectHeader = new Cell().add(new Paragraph("Affectataire").setFontColor(WHITE).setFontSize(8).setBold())
                .setBackgroundColor(MARSA_BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(5).setPaddingRight(5)
                .setBorder(new SolidBorder(MARSA_BLUE, 0.5f));
        table.addHeaderCell(affectHeader);

        for (int col = 0; col < colCount; col++) {
            String cat = presentCategories.get(col);
            AssignmentHistory ah = byCategory.get(cat);
            Equipment eq = ah.getEquipment();
            String content = (eq != null && eq.getName() != null) ? eq.getName() : "-";
            if (eq != null && eq.getSerialNumber() != null && !eq.getSerialNumber().isEmpty()) {
                content += " (SN: " + eq.getSerialNumber() + ")";
            }
            Paragraph p = new Paragraph(content).setFontSize(8).setFontColor(TEXT_DARK);
            Cell c = new Cell().add(p).setPadding(7).setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(BORDER_SOFT, 0.5f))
                    .setBackgroundColor(col % 2 == 0 ? OFF_WHITE : WHITE);
            table.addCell(c);
        }
        
        String affectName = "-";
        if (!assignments.isEmpty() && assignments.get(0).getAffectataire() != null) {
            Affectataire a = assignments.get(0).getAffectataire();
            affectName = a.getFullName() != null && !a.getFullName().isEmpty() ? a.getFullName() : a.getNom();
        }
        Cell last = new Cell().add(new Paragraph(affectName).setFontSize(10).setFontColor(MARSA_BLUE).setBold()).setPadding(7)
                .setBorder(new SolidBorder(BORDER_SOFT, 0.5f)).setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(LIGHT_BG);
        table.addCell(last);

        return table;
    }

    private Table buildPdfSignatureSection(User user) {
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        String userName = user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getUsername();

        Cell leftLabel = new Cell().add(new Paragraph("Entit\u00E9 demandeuse").setFontSize(10).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(8);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la SDBUM").setFontSize(10).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(8);

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(16);
        leftBox.add(new Paragraph("Nom: " + userName).setFontSize(10).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Signature:").setFontSize(10).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Cachet:").setFontSize(10).setFontColor(TEXT_DARK));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(16);
        rightBox.add(new Paragraph("Nom:").setFontSize(10).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Signature:").setFontSize(10).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Cachet:").setFontSize(10).setFontColor(TEXT_DARK));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        return sigTable;
    }

    private Table buildPdfFooter() {
        return new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth()
                .addCell(new Cell().add(new Paragraph("MARSA MAROC").setFontSize(8).setFontColor(MARSA_BLUE).setBold()
                        .setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER).setPaddingTop(14));
    }

    private Table buildSingleEquipmentTable(Equipment equipment, List<AssignmentHistory> assignments) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65})).useAllAvailableWidth();
        table.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        addInfoRow(table, "Cat\u00E9gorie", inferCategory(equipment));
        addInfoRow(table, "Mod\u00E8le", equipment.getModel() != null ? equipment.getModel() : "-");
        addInfoRow(table, "Marque", equipment.getBrand() != null ? equipment.getBrand() : "-");
        addInfoRow(table, "N\u00B0 S\u00E9rie", equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "-");

        String affectataire = "-";
        if (!assignments.isEmpty() && assignments.get(0).getAffectataire() != null) {
            Affectataire a = assignments.get(0).getAffectataire();
            affectataire = a.getFullName() != null && !a.getFullName().isEmpty() ? a.getFullName() : a.getNom();
        }
        addInfoRow(table, "Affectataire", affectataire);

        return table;
    }

    private void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBackgroundColor(LIGHT_BG)
                .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(10).setPaddingRight(10)
                .setBorder(new SolidBorder(BORDER_SOFT, 0.5f));
        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFontSize(9).setFontColor(TEXT_DARK))
                .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(10).setPaddingRight(10)
                .setBorder(new SolidBorder(BORDER_SOFT, 0.5f));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private Table buildEquipmentSignatureSection(List<AssignmentHistory> assignments) {
        String affectataire = "-";
        if (!assignments.isEmpty() && assignments.get(0).getAffectataire() != null) {
            Affectataire a = assignments.get(0).getAffectataire();
            affectataire = a.getFullName() != null && !a.getFullName().isEmpty() ? a.getFullName() : a.getNom();
        }

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        Cell leftLabel = new Cell().add(new Paragraph("Entit\u00E9 demandeuse").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la SDBUM").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        leftBox.add(new Paragraph("Nom: " + affectataire).setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        rightBox.add(new Paragraph("Nom:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        return sigTable;
    }
    
    private Table buildAffectataireSignatureSection(Affectataire affectataire) {
        String affectName = affectataire.getFullName() != null && !affectataire.getFullName().isEmpty() 
                ? affectataire.getFullName() : affectataire.getNom();

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        sigTable.setBorder(new SolidBorder(MARSA_BLUE, 1.2f));

        Cell leftLabel = new Cell().add(new Paragraph("Entit\u00E9 demandeuse").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);
        Cell rightLabel = new Cell().add(new Paragraph("Le Chef de la SDBUM").setFontSize(9).setBold().setFontColor(MARSA_BLUE))
                .setBorder(Border.NO_BORDER).setPadding(7);

        Cell leftBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        leftBox.add(new Paragraph("Nom: " + affectName).setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        leftBox.add(new Paragraph(" ").setFontSize(20));
        leftBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        Cell rightBox = new Cell().setBorder(new SolidBorder(MARSA_BLUE, 1f)).setPadding(14);
        rightBox.add(new Paragraph("Nom:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Signature:").setFontSize(9).setFontColor(TEXT_DARK));
        rightBox.add(new Paragraph(" ").setFontSize(20));
        rightBox.add(new Paragraph("Cachet:").setFontSize(9).setFontColor(TEXT_DARK));

        sigTable.addCell(leftLabel);
        sigTable.addCell(rightLabel);
        sigTable.addCell(leftBox);
        sigTable.addCell(rightBox);

        return sigTable;
    }

    private String inferCategory(Equipment e) {
        if (e == null) return "PORTABLE";
        String m = (e.getModel() != null ? e.getModel().toLowerCase() : "") + " " + (e.getName() != null ? e.getName().toLowerCase() : "");
        if (m.contains("thinkcentre") || m.contains("pc fixe") || m.contains("desktop")) return "PC_FIXE";
        if (m.contains("travel dock") || m.contains("dual display") || m.contains("adaptateur")) return "ADAPTER";
        if ((m.contains("hybrid") || m.contains("station")) && m.contains("dock")) return "DOCKING";
        if (m.contains("ssd") || m.contains("disk") || m.contains("disque") || m.contains("hdd")) return "HDD";
        if (m.contains("thinkpad") || m.contains("legion") || m.contains("laptop") || m.contains("ordinateur")) return "PORTABLE";
        if (m.contains("portable") && !m.contains("ssd")) return "PORTABLE";
        if (m.contains("monitor") || m.contains("\u00E9cran") || m.contains("vision") || m.contains("27q")) return "MONITOR";
        if (m.contains("keyboard") || m.contains("mouse") || m.contains("clavier") || m.contains("souris") || m.contains("mk220") || m.contains("combo")) return "KEYBOARD_MOUSE";
        return "PORTABLE";
    }
}
