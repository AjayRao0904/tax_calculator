package com.accounting.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.accounting.entity.AdvanceTax;
import com.accounting.entity.Asset;
import com.accounting.entity.Company;
import com.accounting.entity.CompanyProfile;
import com.accounting.entity.Expense;
import com.accounting.entity.Expense.ExpenseCategory;
import com.accounting.entity.Income;
import com.accounting.entity.Loss;
import com.accounting.entity.TDSCredit;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.CorporateTaxCalculatorService;
import com.accounting.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Controller
@RequestMapping("/tax")
@Validated
public class DetailedTaxController {
    private final CorporateTaxCalculatorService taxCalculatorService;
    private final CompanyService companyService;
    private final UserService userService;

    public DetailedTaxController(CorporateTaxCalculatorService taxCalculatorService,
                               CompanyService companyService,
                               UserService userService) {
        this.taxCalculatorService = taxCalculatorService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping("/report/{companyId}")
    public String showTaxReport(@PathVariable Long companyId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        User currentUser = getCurrentUser();
        Company company = companyService.getCompany(companyId, currentUser);
        CompanyProfile profile = company.getCompanyProfile();
        
        LocalDate finalStartDate = startDate != null ? startDate : LocalDate.now().withDayOfYear(1);
        LocalDate finalEndDate = endDate != null ? endDate : LocalDate.now();

        List<Expense> expenses = company.getExpenses().stream()
            .filter(e -> !e.getDate().isBefore(finalStartDate) && !e.getDate().isAfter(finalEndDate))
            .collect(Collectors.toList());
        
        List<Income> incomes = company.getIncomes().stream()
            .filter(i -> !i.getDate().isBefore(finalStartDate) && !i.getDate().isAfter(finalEndDate))
            .collect(Collectors.toList());

        Map<ExpenseCategory, BigDecimal> disallowedExpenses = taxCalculatorService.calculateDisallowedExpenses(expenses);
        BigDecimal exemptIncome = taxCalculatorService.calculateExemptIncome(incomes);
        BigDecimal depreciationAdjustment = taxCalculatorService.calculateDepreciationAdjustment(
            company.getAssets(), 
            profile.isNewManufacturingCompany()
        );
        
        List<Loss> broughtForwardLosses = taxCalculatorService.getBroughtForwardLosses(
            company.getLosses(), 
            LocalDate.now().getYear()
        );
        
        BigDecimal tdsCredits = taxCalculatorService.getTDSCredits(company.getTdsCredits());
        BigDecimal advanceTaxPaid = taxCalculatorService.getAdvanceTaxPaid(company.getAdvanceTaxPayments());

        model.addAttribute("company", company);
        model.addAttribute("startDate", finalStartDate);
        model.addAttribute("endDate", finalEndDate);
        model.addAttribute("disallowedExpenses", disallowedExpenses);
        model.addAttribute("exemptIncome", exemptIncome);
        model.addAttribute("depreciationAdjustment", depreciationAdjustment);
        model.addAttribute("broughtForwardLosses", broughtForwardLosses);
        model.addAttribute("tdsCredits", tdsCredits);
        model.addAttribute("advanceTaxPaid", advanceTaxPaid);
        
        // Calculate taxable income and tax liability
        BigDecimal taxableIncome = taxCalculatorService.calculateTaxableIncome(
            expenses,
            incomes,
            company.getAssets(),
            company.getLosses(),
            profile,
            LocalDate.now().getYear()
        );
        BigDecimal taxLiability = taxCalculatorService.calculateTaxAmount(taxableIncome, profile);
        BigDecimal netTaxPayable = taxLiability.subtract(tdsCredits).subtract(advanceTaxPaid);
        
        model.addAttribute("taxableIncome", taxableIncome);
        model.addAttribute("taxLiability", taxLiability);
        model.addAttribute("netTaxPayable", netTaxPayable);
        
        return "tax/detailed-report";
    }

    @PostMapping("/calculate")
    @ResponseBody
    public DetailedTaxResponse calculateTax(@RequestBody @Valid CompanyTaxData data) {
        DetailedTaxResponse response = new DetailedTaxResponse();
        CompanyProfile profile = new CompanyProfile();
        profile.setIncorporationYear(Year.now());
        profile.setStartManufacturingYear(data.isNewManufacturing() ? Year.now() : null);
        profile.setUsesOldMachinery(!data.isNewManufacturing());
        profile.setInRestrictedBusiness(false);
        
        Map<ExpenseCategory, BigDecimal> disallowedExpenses = taxCalculatorService.calculateDisallowedExpenses(data.getExpenses());
        BigDecimal exemptIncome = taxCalculatorService.calculateExemptIncome(data.getIncomes());
        BigDecimal depreciationAdjustment = taxCalculatorService.calculateDepreciationAdjustment(
            data.getAssets(), 
            profile.isNewManufacturingCompany()
        );
        
        List<Loss> broughtForwardLosses = taxCalculatorService.getBroughtForwardLosses(
            data.getLosses(), 
            LocalDate.now().getYear()
        );
        
        BigDecimal tdsCredits = taxCalculatorService.getTDSCredits(data.getTdsCredits());
        BigDecimal advanceTaxPaid = taxCalculatorService.getAdvanceTaxPaid(data.getAdvanceTaxPayments());
        
        BigDecimal taxableIncome = taxCalculatorService.calculateTaxableIncome(
            data.getExpenses(),
            data.getIncomes(),
            data.getAssets(),
            data.getLosses(),
            profile,
            LocalDate.now().getYear()
        );
        
        BigDecimal taxLiability = taxCalculatorService.calculateTaxAmount(taxableIncome, profile);
        BigDecimal netTaxPayable = taxLiability.subtract(tdsCredits).subtract(advanceTaxPaid);
        
        response.setDisallowedExpenses(disallowedExpenses);
        response.setExemptIncome(exemptIncome);
        response.setDepreciationAdjustment(depreciationAdjustment);
        response.setBroughtForwardLosses(broughtForwardLosses);
        response.setTdsCredits(tdsCredits);
        response.setAdvanceTaxPaid(advanceTaxPaid);
        response.setTaxableIncome(taxableIncome);
        response.setTaxLiability(taxLiability);
        response.setNetTaxPayable(netTaxPayable);
        response.setNewManufacturing(data.isNewManufacturing());
        
        return response;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    @Data
    public static class CompanyTaxData {
        @NotNull
        private BigDecimal totalIncome;
        @NotNull
        private BigDecimal totalExpenses;
        private List<Expense> expenses;
        private List<Income> incomes;
        private List<Asset> assets;
        private List<Loss> losses;
        private List<TDSCredit> tdsCredits;
        private List<AdvanceTax> advanceTaxPayments;
        private boolean isNewManufacturing;
    }

    @Data
    public static class DetailedTaxResponse {
        private Map<ExpenseCategory, BigDecimal> disallowedExpenses;
        private BigDecimal exemptIncome;
        private BigDecimal depreciationAdjustment;
        private List<Loss> broughtForwardLosses;
        private BigDecimal tdsCredits;
        private BigDecimal advanceTaxPaid;
        private BigDecimal taxableIncome;
        private BigDecimal taxLiability;
        private BigDecimal netTaxPayable;
        private boolean isNewManufacturing;
    }
} 