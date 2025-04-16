package com.accounting.report;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.accounting.entity.Company;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFReport implements Report {
    private Company company;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal taxRate;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal taxableIncome;
    private BigDecimal taxAmount;

    @Override
    public byte[] generate() {
        try {
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            
            document.open();
            document.add(new Paragraph("Tax Calculation Report"));
            document.add(new Paragraph("Company: " + company.getName()));
            document.add(new Paragraph("Period: " + startDate + " to " + endDate));
            document.add(new Paragraph(""));
            document.add(new Paragraph("Total Revenue: $" + totalRevenue));
            document.add(new Paragraph("Total Expenses: $" + totalExpenses));
            document.add(new Paragraph("Taxable Income: $" + taxableIncome));
            document.add(new Paragraph("Tax Rate: " + taxRate + "%"));
            document.add(new Paragraph("Tax Amount: $" + taxAmount));
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    @Override
    public String getReportName() {
        return "Tax Report - " + company.getName() + " - " + startDate + " to " + endDate;
    }

    @Override
    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    @Override
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    @Override
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    @Override
    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    @Override
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
} 