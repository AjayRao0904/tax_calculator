package com.accounting.report;

public class ReportFactory {
    public enum ReportType {
        PDF,
        EXCEL,
        CSV
    }

    public static Report createReport(ReportType type) {
        switch (type) {
            case PDF:
                return new PDFReport();
            case EXCEL:
                // TODO: Implement ExcelReport
                throw new UnsupportedOperationException("Excel report not implemented yet");
            case CSV:
                // TODO: Implement CSVReport
                throw new UnsupportedOperationException("CSV report not implemented yet");
            default:
                throw new IllegalArgumentException("Unknown report type: " + type);
        }
    }
} 