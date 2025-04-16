package com.accounting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.accounting.entity.TaxCalculation;
import com.accounting.entity.Company;
import com.accounting.repository.TaxCalculationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaxCalculationService {
    private final TaxCalculationRepository taxCalculationRepository;

    public TaxCalculationService(TaxCalculationRepository taxCalculationRepository) {
        this.taxCalculationRepository = taxCalculationRepository;
    }

    @Transactional
    public TaxCalculation saveTaxCalculation(TaxCalculation taxCalculation) {
        taxCalculation.setCalculationDate(LocalDate.now());
        return taxCalculationRepository.save(taxCalculation);
    }

    public List<TaxCalculation> getCompanyTaxCalculations(Company company) {
        return taxCalculationRepository.findByCompanyOrderByCalculationDateDesc(company);
    }

    public List<TaxCalculation> getCompanyTaxCalculationsForPeriod(
            Company company, LocalDate startDate, LocalDate endDate) {
        return taxCalculationRepository.findByCompanyAndStartDateBetweenOrderByCalculationDateDesc(
            company, startDate, endDate);
    }

    public TaxCalculation calculateTax(
            Company company,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal totalRevenue,
            BigDecimal totalExpenses,
            BigDecimal assetCosts,
            BigDecimal depreciationRate,
            BigDecimal taxRate,
            BigDecimal tdsCredits,
            BigDecimal advanceTax) {

        // Create new tax calculation instance
        TaxCalculation calc = new TaxCalculation();
        calc.setCompany(company);
        calc.setStartDate(startDate);
        calc.setEndDate(endDate);
        calc.setTotalRevenue(totalRevenue);
        calc.setTotalExpenses(totalExpenses);
        calc.setAssetCosts(assetCosts);
        calc.setDepreciationRate(depreciationRate);
        calc.setTaxRate(taxRate);
        calc.setTdsCredits(tdsCredits);
        calc.setAdvanceTax(advanceTax);

        // Calculate depreciation
        BigDecimal depreciation = assetCosts.multiply(depreciationRate.divide(BigDecimal.valueOf(100)));
        calc.setDepreciation(depreciation);

        // Calculate taxable income
        BigDecimal taxableIncome = totalRevenue.subtract(totalExpenses).subtract(depreciation);
        calc.setTaxableIncome(taxableIncome);

        // Calculate gross tax amount
        BigDecimal grossTaxAmount = taxableIncome.multiply(taxRate.divide(BigDecimal.valueOf(100)));
        calc.setGrossTaxAmount(grossTaxAmount);

        // Calculate net tax payable
        BigDecimal netTaxPayable = grossTaxAmount.subtract(tdsCredits).subtract(advanceTax);
        calc.setNetTaxPayable(netTaxPayable);

        return saveTaxCalculation(calc);
    }
} 