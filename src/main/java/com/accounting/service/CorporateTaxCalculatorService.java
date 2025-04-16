package com.accounting.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.accounting.entity.AdvanceTax;
import com.accounting.entity.Asset;
import com.accounting.entity.CompanyProfile;
import com.accounting.entity.Expense;
import com.accounting.entity.Expense.ExpenseCategory;
import com.accounting.entity.Income;
import com.accounting.entity.Income.IncomeCategory;
import com.accounting.entity.Loss;
import com.accounting.entity.TDSCredit;

@Service
public class CorporateTaxCalculatorService {

    public Map<ExpenseCategory, BigDecimal> calculateDisallowedExpenses(List<Expense> expenses) {
        return expenses.stream()
            .filter(expense -> isDisallowedExpense(expense))
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));
    }

    private boolean isDisallowedExpense(Expense expense) {
        return expense.getCategory() == ExpenseCategory.PENALTY_FINES ||
               expense.getCategory() == ExpenseCategory.DONATIONS_CSR ||
               expense.getCategory() == ExpenseCategory.ENTERTAINMENT ||
               expense.getCategory() == ExpenseCategory.CLUB_MEMBERSHIPS ||
               expense.getCategory() == ExpenseCategory.POLITICAL_CONTRIBUTIONS ||
               (expense.getPaymentMode() == Expense.PaymentMode.CASH && 
                expense.getAmount().compareTo(new BigDecimal("10000")) > 0);
    }

    public BigDecimal calculateExemptIncome(List<Income> incomes) {
        return incomes.stream()
            .filter(income -> isExemptIncome(income))
            .map(Income::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isExemptIncome(Income income) {
        return income.getCategory() == IncomeCategory.AGRICULTURAL_INCOME ||
               income.getCategory() == IncomeCategory.DIVIDENDS ||
               income.getCategory() == IncomeCategory.PARTNERSHIP_PROFIT ||
               income.getCategory() == IncomeCategory.SEZ_INCOME ||
               income.getCategory() == IncomeCategory.LONG_TERM_CAPITAL_GAINS;
    }

    public BigDecimal calculateDepreciationAdjustment(List<Asset> assets, boolean isNewManufacturing) {
        BigDecimal totalDepreciation = assets.stream()
            .map(Asset::calculateDepreciation)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (isNewManufacturing) {
            BigDecimal additionalDepreciation = assets.stream()
                .map(Asset::calculateAdditionalDepreciation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalDepreciation = totalDepreciation.add(additionalDepreciation);
        }

        return totalDepreciation;
    }

    public List<Loss> getBroughtForwardLosses(List<Loss> losses, int currentYear) {
        return losses.stream()
            .filter(loss -> !loss.isExpired(currentYear))
            .sorted((l1, l2) -> l1.getYear().compareTo(l2.getYear()))
            .collect(Collectors.toList());
    }

    public BigDecimal getTDSCredits(List<TDSCredit> credits) {
        return credits.stream()
            .map(TDSCredit::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAdvanceTaxPaid(List<AdvanceTax> payments) {
        return payments.stream()
            .map(AdvanceTax::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isNewManufacturingCompany(CompanyProfile profile) {
        return profile.isNewManufacturingCompany();
    }

    public BigDecimal calculateTaxableIncome(
            List<Expense> expenses,
            List<Income> incomes,
            List<Asset> assets,
            List<Loss> losses,
            CompanyProfile profile,
            int currentYear) {

        // Calculate total income
        BigDecimal totalIncome = incomes.stream()
            .filter(income -> !isExemptIncome(income))
            .map(Income::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total allowed expenses
        BigDecimal totalAllowedExpenses = expenses.stream()
            .filter(expense -> !isDisallowedExpense(expense))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate depreciation
        BigDecimal depreciation = calculateDepreciationAdjustment(
            assets, isNewManufacturingCompany(profile));

        // Calculate taxable income before losses
        BigDecimal taxableIncome = totalIncome
            .subtract(totalAllowedExpenses)
            .subtract(depreciation);

        // Apply brought forward losses
        List<Loss> validLosses = getBroughtForwardLosses(losses, currentYear);
        for (Loss loss : validLosses) {
            BigDecimal unutilizedAmount = loss.getUnutilizedAmount();
            if (unutilizedAmount.compareTo(BigDecimal.ZERO) > 0) {
                if (taxableIncome.compareTo(unutilizedAmount) >= 0) {
                    taxableIncome = taxableIncome.subtract(unutilizedAmount);
                    loss.setUtilizedAmount(loss.getAmount());
                } else {
                    loss.setUtilizedAmount(loss.getUtilizedAmount().add(taxableIncome));
                    taxableIncome = BigDecimal.ZERO;
                    break;
                }
            }
        }

        return taxableIncome;
    }

    public BigDecimal calculateTaxAmount(BigDecimal taxableIncome, CompanyProfile profile) {
        BigDecimal taxRate;
        
        if (profile.isNewManufacturingCompany()) {
            taxRate = new BigDecimal("0.15"); // 15% for new manufacturing companies
        } else {
            taxRate = new BigDecimal("0.25"); // 25% for other companies
        }

        return taxableIncome.multiply(taxRate);
    }
} 