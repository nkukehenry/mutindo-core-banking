package com.mutindo.reporting.export;

import com.mutindo.reporting.dto.ReportResponse;

import java.util.List;
import java.util.Map;

/**
 * Export service interface for different output formats
 * Follows our established pattern of interface-driven design
 */
public interface IExportService {
    
    /**
     * Export data to PDF format
     * @param data Report data
     * @param reportName Report name
     * @param parameters Report parameters
     * @return Report response with file path
     */
    ReportResponse exportToPdf(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters);
    
    /**
     * Export data to Excel format
     * @param data Report data
     * @param reportName Report name
     * @param parameters Report parameters
     * @return Report response with file path
     */
    ReportResponse exportToExcel(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters);
    
    /**
     * Export data to CSV format
     * @param data Report data
     * @param reportName Report name
     * @param parameters Report parameters
     * @return Report response with file path
     */
    ReportResponse exportToCsv(List<Map<String, Object>> data, String reportName, Map<String, Object> parameters);
    
    /**
     * Get supported output formats
     * @return List of supported formats
     */
    List<String> getSupportedFormats();
}
