package com.accounting.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "tax_calculations")
public class TaxCalculation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalExpenses;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal assetCosts;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal depreciationRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal depreciation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxableIncome;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grossTaxAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tdsCredits;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal advanceTax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netTaxPayable;

    @Column(nullable = false)
    private LocalDate calculationDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getAssetCosts() {
        return assetCosts;
    }

    public void setAssetCosts(BigDecimal assetCosts) {
        this.assetCosts = assetCosts;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public BigDecimal getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(BigDecimal depreciation) {
        this.depreciation = depreciation;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getGrossTaxAmount() {
        return grossTaxAmount;
    }

    public void setGrossTaxAmount(BigDecimal grossTaxAmount) {
        this.grossTaxAmount = grossTaxAmount;
    }

    public BigDecimal getTdsCredits() {
        return tdsCredits;
    }

    public void setTdsCredits(BigDecimal tdsCredits) {
        this.tdsCredits = tdsCredits;
    }

    public BigDecimal getAdvanceTax() {
        return advanceTax;
    }

    public void setAdvanceTax(BigDecimal advanceTax) {
        this.advanceTax = advanceTax;
    }

    public BigDecimal getNetTaxPayable() {
        return netTaxPayable;
    }

    public void setNetTaxPayable(BigDecimal netTaxPayable) {
        this.netTaxPayable = netTaxPayable;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }
} 