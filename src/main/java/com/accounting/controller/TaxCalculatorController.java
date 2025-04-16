package com.accounting.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.accounting.entity.Company;
import com.accounting.entity.User;
import com.accounting.report.Report;
import com.accounting.report.ReportFactory;
import com.accounting.service.CompanyService;
import com.accounting.service.ExpenseService;
import com.accounting.service.RevenueService;
import com.accounting.service.TaxCalculationService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/tax-calculator")
public class TaxCalculatorController {
    private static final Logger logger = LoggerFactory.getLogger(TaxCalculatorController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final CompanyService companyService;
    private final RevenueService revenueService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final TaxCalculationService taxCalculationService;

    public TaxCalculatorController(CompanyService companyService, RevenueService revenueService,
            ExpenseService expenseService, UserService userService, TaxCalculationService taxCalculationService) {
        this.companyService = companyService;
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.taxCalculationService = taxCalculationService;
    }

    @GetMapping
    public String showCalculator(Model model) {
        try {
            User user = getCurrentUser();
            List<Company> companies = companyService.getUserCompanies(user);
            model.addAttribute("companies", companies);
            model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
            model.addAttribute("endDate", LocalDate.now());
            return "tax-calculator/calculator";
        } catch (Exception e) {
            logger.error("Error showing calculator: ", e);
            model.addAttribute("error", true);
            model.addAttribute("message", "Error loading calculator: " + e.getMessage());
            return "tax-calculator/calculator";
        }
    }

    @PostMapping("/calculate")
    public String calculateTax(
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "20") BigDecimal taxRate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "0") BigDecimal tdsCredits,
            @RequestParam(required = false, defaultValue = "0") BigDecimal assetCosts,
            @RequestParam(required = false, defaultValue = "0") BigDecimal advanceTax,
            @RequestParam(required = false, defaultValue = "10") BigDecimal depreciationRate,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate inputs
            if (companyId == null) {
                throw new IllegalArgumentException("Company must be selected");
            }
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date are required");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            
            User user = getCurrentUser();
            Company company = companyService.getCompany(companyId, user);
            if (company == null) {
                throw new IllegalArgumentException("Invalid company selected");
            }

            // Format dates with forward slashes in DD/MM/YYYY format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedStartDate = startDate.format(formatter);
            String formattedEndDate = endDate.format(formatter);

            logger.debug("Formatted dates for redirect - start: '{}', end: '{}'", formattedStartDate, formattedEndDate);

            // Add all parameters to redirect attributes
            redirectAttributes.addAttribute("companyId", companyId);
            redirectAttributes.addAttribute("taxRate", taxRate);
            redirectAttributes.addAttribute("startDate", formattedStartDate);
            redirectAttributes.addAttribute("endDate", formattedEndDate);
            redirectAttributes.addAttribute("tdsCredits", tdsCredits);
            redirectAttributes.addAttribute("assetCosts", assetCosts);
            redirectAttributes.addAttribute("advanceTax", advanceTax);
            redirectAttributes.addAttribute("depreciationRate", depreciationRate);

            return "redirect:/tax/report";
        } catch (Exception e) {
            logger.error("Error calculating tax: ", e);
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/tax-calculator";
        }
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadTaxReport(@RequestParam Long companyId,
                                                  @RequestParam BigDecimal taxRate,
                                                  @RequestParam LocalDate startDate,
                                                  @RequestParam LocalDate endDate) {
        try {
            User user = getCurrentUser();
            Company company = companyService.getCompany(companyId, user);
            
            BigDecimal totalRevenue = revenueService.getCompanyRevenuesByDateRange(companyId, startDate, endDate, user)
                .stream()
                .map(revenue -> revenue.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpenses = expenseService.getCompanyExpensesByDateRange(companyId, startDate, endDate, user)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal taxableIncome = totalRevenue.subtract(totalExpenses);
            BigDecimal taxAmount = taxableIncome.multiply(taxRate.divide(BigDecimal.valueOf(100)));

            // Use the factory to create a PDF report
            Report report = ReportFactory.createReport(ReportFactory.ReportType.PDF);
            report.setCompany(company);
            report.setDateRange(startDate, endDate);
            report.setTaxRate(taxRate);
            report.setTotalRevenue(totalRevenue);
            report.setTotalExpenses(totalExpenses);
            report.setTaxableIncome(taxableIncome);
            report.setTaxAmount(taxAmount);

            byte[] pdfContent = report.generate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tax-report.pdf");

            return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 