package com.farm.management.controller;

import com.farm.management.entity.*;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CropService cropService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private HarvestRecordService harvestRecordService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ExpenseService expenseService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Download PDF Report
    @GetMapping("/pdf")
    public void downloadPDF(HttpServletResponse response) throws Exception {
        User user = getCurrentUser();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=farm-report-" +
                        LocalDate.now() + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Title
        com.itextpdf.text.Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 20,
                new BaseColor(26, 92, 42));
        com.itextpdf.text.Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12,
                new BaseColor(255, 255, 255));
        com.itextpdf.text.Font normalFont = FontFactory.getFont(
                FontFactory.HELVETICA, 10,
                BaseColor.BLACK);

        Paragraph title = new Paragraph(
                "Farm Management System — Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(
                "Farmer: " + user.getFullName() +
                        " | Date: " + LocalDate.now(), normalFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Crops Section
        addSectionTitle(document, "MAZAO (CROPS)",
                titleFont, headerFont);
        List<Crop> crops = cropService.findByUser(user);
        PdfPTable cropTable = new PdfPTable(5);
        cropTable.setWidthPercentage(100);
        addTableHeader(cropTable, headerFont,
                "Jina", "Aina", "Kupanda", "Kuvuna", "Hali");
        for (Crop c : crops) {
            cropTable.addCell(new Phrase(c.getCropName(), normalFont));
            cropTable.addCell(new Phrase(
                    c.getCropType() != null ? c.getCropType() : "-", normalFont));
            cropTable.addCell(new Phrase(
                    c.getPlantingDate() != null ?
                            c.getPlantingDate().toString() : "-", normalFont));
            cropTable.addCell(new Phrase(
                    c.getExpectedHarvestDate() != null ?
                            c.getExpectedHarvestDate().toString() : "-", normalFont));
            cropTable.addCell(new Phrase(
                    c.getStatus() != null ? c.getStatus() : "-", normalFont));
        }
        document.add(cropTable);
        document.add(new Paragraph(" "));

        // Inventory Section
        addSectionTitle(document, "VIFAA VYA SHAMBA (INVENTORY)",
                titleFont, headerFont);
        List<Inventory> inventory = inventoryService.findByUser(user);
        PdfPTable invTable = new PdfPTable(4);
        invTable.setWidthPercentage(100);
        addTableHeader(invTable, headerFont,
                "Jina", "Kategoria", "Kiasi", "Kipimo");
        for (Inventory i : inventory) {
            invTable.addCell(new Phrase(i.getItemName(), normalFont));
            invTable.addCell(new Phrase(
                    i.getCategory() != null ? i.getCategory() : "-", normalFont));
            invTable.addCell(new Phrase(
                    i.getQuantity() != null ?
                            i.getQuantity().toString() : "-", normalFont));
            invTable.addCell(new Phrase(
                    i.getUnit() != null ? i.getUnit() : "-", normalFont));
        }
        document.add(invTable);
        document.add(new Paragraph(" "));

        // Finance Summary
        addSectionTitle(document, "MUHTASARI WA FEDHA (FINANCE)",
                titleFont, headerFont);
        PdfPTable finTable = new PdfPTable(2);
        finTable.setWidthPercentage(60);
        finTable.addCell(new Phrase("Mapato Yote (Revenue)", normalFont));
        finTable.addCell(new Phrase("TZS " +
                saleService.getTotalRevenue(user).toPlainString(), normalFont));
        finTable.addCell(new Phrase("Matumizi Yote (Expenses)", normalFont));
        finTable.addCell(new Phrase("TZS " +
                expenseService.getTotalExpenses(user).toPlainString(), normalFont));
        finTable.addCell(new Phrase("Faida/Hasara (Net Profit)", normalFont));
        finTable.addCell(new Phrase("TZS " +
                saleService.getTotalRevenue(user)
                        .subtract(expenseService.getTotalExpenses(user))
                        .toPlainString(), normalFont));
        document.add(finTable);

        document.close();
    }

    // Download Excel Report
    @GetMapping("/excel")
    public void downloadExcel(HttpServletResponse response) throws Exception {
        User user = getCurrentUser();
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=farm-report-" +
                        LocalDate.now() + ".xlsx");

        Workbook workbook = new XSSFWorkbook();

        // Style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        // Crops Sheet
        Sheet cropSheet = workbook.createSheet("Mazao");
        Row cropHeader = cropSheet.createRow(0);
        String[] cropCols = {"Jina", "Aina", "Kupanda",
                "Kuvuna", "Mahali", "Hali"};
        for (int i = 0; i < cropCols.length; i++) {
            Cell cell = cropHeader.createCell(i);
            cell.setCellValue(cropCols[i]);
            cell.setCellStyle(headerStyle);
        }
        List<Crop> crops = cropService.findByUser(user);
        int row = 1;
        for (Crop c : crops) {
            Row r = cropSheet.createRow(row++);
            r.createCell(0).setCellValue(c.getCropName());
            r.createCell(1).setCellValue(
                    c.getCropType() != null ? c.getCropType() : "");
            r.createCell(2).setCellValue(
                    c.getPlantingDate() != null ?
                            c.getPlantingDate().toString() : "");
            r.createCell(3).setCellValue(
                    c.getExpectedHarvestDate() != null ?
                            c.getExpectedHarvestDate().toString() : "");
            r.createCell(4).setCellValue(
                    c.getFieldLocation() != null ?
                            c.getFieldLocation() : "");
            r.createCell(5).setCellValue(
                    c.getStatus() != null ? c.getStatus() : "");
        }

        // Inventory Sheet
        Sheet invSheet = workbook.createSheet("Vifaa");
        Row invHeader = invSheet.createRow(0);
        String[] invCols = {"Jina", "Kategoria", "Kiasi", "Kipimo", "Msambazaji"};
        for (int i = 0; i < invCols.length; i++) {
            Cell cell = invHeader.createCell(i);
            cell.setCellValue(invCols[i]);
            cell.setCellStyle(headerStyle);
        }
        List<Inventory> inventory = inventoryService.findByUser(user);
        row = 1;
        for (Inventory inv : inventory) {
            Row r = invSheet.createRow(row++);
            r.createCell(0).setCellValue(inv.getItemName());
            r.createCell(1).setCellValue(
                    inv.getCategory() != null ? inv.getCategory() : "");
            r.createCell(2).setCellValue(
                    inv.getQuantity() != null ?
                            inv.getQuantity().doubleValue() : 0);
            r.createCell(3).setCellValue(
                    inv.getUnit() != null ? inv.getUnit() : "");
            r.createCell(4).setCellValue(
                    inv.getSupplier() != null ? inv.getSupplier() : "");
        }

        // Finance Sheet
        Sheet finSheet = workbook.createSheet("Fedha");
        Row finHeader = finSheet.createRow(0);
        String[] finCols = {"Kipengele", "Kiasi (TZS)"};
        for (int i = 0; i < finCols.length; i++) {
            Cell cell = finHeader.createCell(i);
            cell.setCellValue(finCols[i]);
            cell.setCellStyle(headerStyle);
        }
        Row r1 = finSheet.createRow(1);
        r1.createCell(0).setCellValue("Mapato Yote");
        r1.createCell(1).setCellValue(
                saleService.getTotalRevenue(user).doubleValue());
        Row r2 = finSheet.createRow(2);
        r2.createCell(0).setCellValue("Matumizi Yote");
        r2.createCell(1).setCellValue(
                expenseService.getTotalExpenses(user).doubleValue());
        Row r3 = finSheet.createRow(3);
        r3.createCell(0).setCellValue("Faida/Hasara");
        r3.createCell(1).setCellValue(
                saleService.getTotalRevenue(user)
                        .subtract(expenseService.getTotalExpenses(user)).doubleValue());

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void addSectionTitle(Document doc, String title,
                                 com.itextpdf.text.Font titleFont,
                                 com.itextpdf.text.Font headerFont) throws DocumentException {
        com.itextpdf.text.Font sectionFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 13,
                new BaseColor(26, 92, 42));
        Paragraph p = new Paragraph(title, sectionFont);
        p.setSpacingBefore(15);
        p.setSpacingAfter(8);
        doc.add(p);
    }

    private void addTableHeader(PdfPTable table,
                                com.itextpdf.text.Font headerFont, String... cols) {
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(new BaseColor(26, 92, 42));
            cell.setPadding(6);
            table.addCell(cell);
        }
    }
}
