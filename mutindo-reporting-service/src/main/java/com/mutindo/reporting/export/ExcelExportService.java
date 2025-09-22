package com.mutindo.reporting.export;

import com.mutindo.reporting.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

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
 * Excel Export Service implementation
 * Reuses existing infrastructure: Logging, File System
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService implements IExportService {

    private static final String REPORTS_DIR = "reports/excel";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public ReportResponse exportToExcel(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        log.info("Exporting report to Excel: {} - Rows: {}", reportName, data.size());

        try {
            // Create reports directory if it doesn't exist
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String filename = String.format("%s_%s_%s.xlsx", 
                reportName.replaceAll("[^a-zA-Z0-9]", "_"), 
                timestamp, 
                UUID.randomUUID().toString().substring(0, 8));
            
            String filePath = Paths.get(REPORTS_DIR, filename).toString();

            // Generate Excel content
            generateExcelContent(data, reportName, parameters, filePath);

            // Get file size
            long fileSize = Files.size(Paths.get(filePath));

            log.info("Excel report generated successfully: {} - Size: {} bytes", filePath, fileSize);

            return ReportResponse.builder()
                    .reportName(reportName)
                    .outputFormat("EXCEL")
                    .filePath(filePath)
                    .fileSize(fileSize)
                    .rowsReturned(data.size())
                    .executionStartTime(LocalDateTime.now())
                    .executionEndTime(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

        } catch (Exception e) {
            log.error("Failed to export report to Excel: {}", reportName, e);
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponse exportToPdf(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("PDF export not supported by Excel service");
    }

    @Override
    public ReportResponse exportToCsv(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("CSV export not supported by Excel service");
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("EXCEL");
    }

    /**
     * Generate Excel content from data
     */
    private void generateExcelContent(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath)) {

            // Create sheet
            Sheet sheet = workbook.createSheet(reportName);

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            int rowNum = 0;

            // Add report header
            Row headerRow = sheet.createRow(rowNum++);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue(reportName);
            headerCell.setCellStyle(headerStyle);

            // Add generation timestamp
            Row timestampRow = sheet.createRow(rowNum++);
            Cell timestampCell = timestampRow.createCell(0);
            timestampCell.setCellValue("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Empty row
            rowNum++;

            if (data.isEmpty()) {
                Row noDataRow = sheet.createRow(rowNum++);
                Cell noDataCell = noDataRow.createCell(0);
                noDataCell.setCellValue("No data available for this report.");
                return;
            }

            // Get column headers from first row
            Map<String, Object> firstRow = data.get(0);
            String[] headers = firstRow.keySet().toArray(new String[0]);

            // Create header row
            Row dataHeaderRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = dataHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            for (Map<String, Object> row : data) {
                Row excelRow = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = excelRow.createCell(i);
                    Object value = row.get(headers[i]);
                    
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof LocalDateTime) {
                            cell.setCellValue((LocalDateTime) value);
                            cell.setCellStyle(dateStyle);
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary
            Row summaryRow = sheet.createRow(rowNum++);
            Cell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("Total Records: " + data.size());
            summaryCell.setCellStyle(headerStyle);

            workbook.write(fileOut);
        }
    }
}
