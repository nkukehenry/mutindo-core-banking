package com.mutindo.reporting.export;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.mutindo.reporting.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PDF Export Service implementation
 * Reuses existing infrastructure: Logging, File System
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService implements IExportService {

    private final TemplateEngine templateEngine;
    private static final String REPORTS_DIR = "reports/pdf";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public ReportResponse exportToPdf(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        log.info("Exporting report to PDF: {} - Rows: {}", reportName, data.size());

        try {
            // Create reports directory if it doesn't exist
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String filename = String.format("%s_%s_%s.pdf", 
                reportName.replaceAll("[^a-zA-Z0-9]", "_"), 
                timestamp, 
                UUID.randomUUID().toString().substring(0, 8));
            
            String filePath = Paths.get(REPORTS_DIR, filename).toString();

            // Generate PDF content
            byte[] pdfContent = generatePdfContent(data, reportName, parameters);

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfContent);
            }

            log.info("PDF report generated successfully: {} - Size: {} bytes", filePath, pdfContent.length);

            return ReportResponse.builder()
                    .reportName(reportName)
                    .outputFormat("PDF")
                    .filePath(filePath)
                    .fileSize((long) pdfContent.length)
                    .rowsReturned(data.size())
                    .executionStartTime(LocalDateTime.now())
                    .executionEndTime(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

        } catch (Exception e) {
            log.error("Failed to export report to PDF: {}", reportName, e);
            throw new RuntimeException("PDF export failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponse exportToExcel(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("Excel export not supported by PDF service");
    }

    @Override
    public ReportResponse exportToCsv(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("CSV export not supported by PDF service");
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("PDF");
    }

    /**
     * Generate PDF content from data
     */
    private byte[] generatePdfContent(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Add report header
            document.add(new Paragraph(reportName).setFontSize(18).setBold());
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(10));
            document.add(new Paragraph(" ")); // Empty line

            if (data.isEmpty()) {
                document.add(new Paragraph("No data available for this report."));
                return baos.toByteArray();
            }

            // Get column headers from first row
            Map<String, Object> firstRow = data.get(0);
            String[] headers = firstRow.keySet().toArray(new String[0]);

            // Create table
            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();

            // Add header row
            for (String header : headers) {
                table.addHeaderCell(new Paragraph(header).setBold());
            }

            // Add data rows
            for (Map<String, Object> row : data) {
                for (String header : headers) {
                    Object value = row.get(header);
                    String cellValue = value != null ? value.toString() : "";
                    table.addCell(new Paragraph(cellValue));
                }
            }

            document.add(table);

            // Add summary
            document.add(new Paragraph(" ")); // Empty line
            document.add(new Paragraph("Total Records: " + data.size()).setFontSize(10));

            return baos.toByteArray();
        }
    }
}
