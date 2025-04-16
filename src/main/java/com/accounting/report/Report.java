package com.accounting.report;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.accounting.entity.Company;

public interface Report {
    byte[] generate();
    String getReportName();
    void setCompany(Company company);
    void setDateRange(LocalDate startDate, LocalDate endDate);
    void setTaxRate(BigDecimal taxRate);
    void setTotalRevenue(BigDecimal totalRevenue);
    void setTotalExpenses(BigDecimal totalExpenses);
    void setTaxableIncome(BigDecimal taxableIncome);
    void setTaxAmount(BigDecimal taxAmount);
} 