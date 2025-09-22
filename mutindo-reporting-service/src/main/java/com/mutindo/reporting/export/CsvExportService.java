package com.mutindo.reporting.export;

import com.mutindo.reporting.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
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
 * CSV Export Service implementation
 * Reuses existing infrastructure: Logging, File System
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CsvExportService implements IExportService {

    private static final String REPORTS_DIR = "reports/csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public ReportResponse exportToCsv(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        log.info("Exporting report to CSV: {} - Rows: {}", reportName, data.size());

        try {
            // Create reports directory if it doesn't exist
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String filename = String.format("%s_%s_%s.csv", 
                reportName.replaceAll("[^a-zA-Z0-9]", "_"), 
                timestamp, 
                UUID.randomUUID().toString().substring(0, 8));
            
            String filePath = Paths.get(REPORTS_DIR, filename).toString();

            // Generate CSV content
            generateCsvContent(data, reportName, parameters, filePath);

            // Get file size
            long fileSize = Files.size(Paths.get(filePath));

            log.info("CSV report generated successfully: {} - Size: {} bytes", filePath, fileSize);

            return ReportResponse.builder()
                    .reportName(reportName)
                    .outputFormat("CSV")
                    .filePath(filePath)
                    .fileSize(fileSize)
                    .rowsReturned(data.size())
                    .executionStartTime(LocalDateTime.now())
                    .executionEndTime(LocalDateTime.now())
                    .status("COMPLETED")
                    .build();

        } catch (Exception e) {
            log.error("Failed to export report to CSV: {}", reportName, e);
            throw new RuntimeException("CSV export failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponse exportToPdf(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("PDF export not supported by CSV service");
    }

    @Override
    public ReportResponse exportToExcel(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters) {
        throw new UnsupportedOperationException("Excel export not supported by CSV service");
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("CSV");
    }

    /**
     * Generate CSV content from data
     */
    private void generateCsvContent(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write report header
            writer.append("Report: ").append(reportName).append("\n");
            writer.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            writer.append("\n");

            if (data.isEmpty()) {
                writer.append("No data available for this report.\n");
                return;
            }

            // Get column headers from first row
            Map<String, Object> firstRow = data.get(0);
            String[] headers = firstRow.keySet().toArray(new String[0]);

            // Write header row
            for (int i = 0; i < headers.length; i++) {
                if (i > 0) {
                    writer.append(",");
                }
                writer.append(escapeCsvValue(headers[i]));
            }
            writer.append("\n");

            // Write data rows
            for (Map<String, Object> row : data) {
                for (int i = 0; i < headers.length; i++) {
                    if (i > 0) {
                        writer.append(",");
                    }
                    Object value = row.get(headers[i]);
                    String cellValue = value != null ? value.toString() : "";
                    writer.append(escapeCsvValue(cellValue));
                }
                writer.append("\n");
            }

            // Write summary
            writer.append("\n");
            writer.append("Total Records: ").append(String.valueOf(data.size())).append("\n");
        }
    }

    /**
     * Escape CSV value to handle commas, quotes, and newlines
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
