package com.accounting.controller;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.accounting.entity.Company;
import com.accounting.entity.TaxCalculation;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.ExpenseService;
import com.accounting.service.RevenueService;
import com.accounting.service.TaxCalculationService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/tax")
public class TaxViewController {
    private static final Logger logger = LoggerFactory.getLogger(TaxViewController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CompanyService companyService;
    private final RevenueService revenueService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final TaxCalculationService taxCalculationService;

    public TaxViewController(CompanyService companyService, 
                           RevenueService revenueService,
                           ExpenseService expenseService, 
                           UserService userService,
                           TaxCalculationService taxCalculationService) {
        this.companyService = companyService;
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.taxCalculationService = taxCalculationService;
    }

    @GetMapping("/calculator")
    public String showCalculator(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName());
            model.addAttribute("companies", companyService.getUserCompanies(user));
            model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
            model.addAttribute("endDate", LocalDate.now());
            return "tax-calculator/calculator";
        } catch (Exception e) {
            logger.error("Error showing calculator: ", e);
            model.addAttribute("error", true);
            model.addAttribute("message", "Error loading calculator: " + e.getMessage());
            return "error";
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be empty");
        }

        try {
            // URL decode and clean up the date string
            dateStr = URLDecoder.decode(dateStr, StandardCharsets.UTF_8)
                              .trim()
                              .replace("%2F", "/")
                              .replace("%2f", "/")
                              .replace("-", "/");

            logger.debug("Attempting to parse date string: '{}'", dateStr);

            // Try multiple date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
            };

            // Try each formatter
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    // Continue to next formatter
                    logger.debug("Failed to parse '{}' with formatter {}", dateStr, formatter);
                }
            }

            // If none of the formatters worked, try to parse the components
            String[] parts = dateStr.split("[/.-]");
            if (parts.length == 3) {
                try {
                    // Try to determine the format based on the values
                    int first = Integer.parseInt(parts[0]);
                    int second = Integer.parseInt(parts[1]);
                    int third = Integer.parseInt(parts[2]);

                    // Pad year if needed
                    if (third < 100) {
                        third += 2000;
                    }

                    // If first number is greater than 12, it's likely a day
                    if (first > 12) {
                        return LocalDate.of(third, second, first);
                    }
                    // If second number is greater than 12, first must be month
                    else if (second > 12) {
                        return LocalDate.of(third, first, second);
                    }
                    // Default to DD/MM/YYYY format
                    else {
                        return LocalDate.of(third, second, first);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse date components: {}", e.getMessage());
                }
            }

            throw new IllegalArgumentException(
                "Invalid date format. Please use DD/MM/YYYY format (e.g., 01/04/2024). " +
                "The date provided was: " + dateStr
            );
        } catch (Exception e) {
            logger.error("Error parsing date '{}': {}", dateStr, e.getMessage());
            throw new IllegalArgumentException(
                "Invalid date format. Please use DD/MM/YYYY format (e.g., 01/04/2024). " +
                "The date provided was: " + dateStr
            );
        }
    }

    @GetMapping("/report")
    public String showTaxReport(
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "20") BigDecimal taxRate,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "0") BigDecimal tdsCredits,
            @RequestParam(required = false, defaultValue = "0") BigDecimal assetCosts,
            @RequestParam(required = false, defaultValue = "0") BigDecimal advanceTax,
            @RequestParam(required = false, defaultValue = "10") BigDecimal depreciationRate,
            Model model) {
        
        try {
            logger.info("Received tax report request for company {} with dates: startDate='{}', endDate='{}'", 
                       companyId, startDate, endDate);
            
            // Parse dates
            LocalDate parsedStartDate = parseDate(startDate);
            LocalDate parsedEndDate = parseDate(endDate);
            
            logger.info("Successfully parsed dates: startDate={}, endDate={}", parsedStartDate, parsedEndDate);
            
            // Input validation
            if (parsedStartDate.isAfter(parsedEndDate)) {
                model.addAttribute("error", true);
                model.addAttribute("message", "Start date cannot be after end date");
                return "error";
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findByUsername(auth.getName());
            Company company = companyService.getCompany(companyId, user);
            
            if (company == null) {
                model.addAttribute("error", true);
                model.addAttribute("message", "Company not found");
                return "error";
            }
            
            // Calculate total revenue
            BigDecimal totalRevenue = revenueService.getCompanyRevenuesByDateRange(companyId, parsedStartDate, parsedEndDate, user)
                .stream()
                .map(revenue -> revenue.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total expenses
            BigDecimal totalExpenses = expenseService.getCompanyExpensesByDateRange(companyId, parsedStartDate, parsedEndDate, user)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate and save tax calculation
            TaxCalculation taxCalculation = taxCalculationService.calculateTax(
                company, parsedStartDate, parsedEndDate, totalRevenue, totalExpenses,
                assetCosts, depreciationRate, taxRate, tdsCredits, advanceTax
            );

            // Add attributes to model
            model.addAttribute("company", company);
            model.addAttribute("startDate", parsedStartDate);
            model.addAttribute("endDate", parsedEndDate);
            model.addAttribute("totalRevenue", taxCalculation.getTotalRevenue());
            model.addAttribute("totalExpenses", taxCalculation.getTotalExpenses());
            model.addAttribute("assetCosts", taxCalculation.getAssetCosts());
            model.addAttribute("depreciation", taxCalculation.getDepreciation());
            model.addAttribute("depreciationRate", taxCalculation.getDepreciationRate());
            model.addAttribute("taxableIncome", taxCalculation.getTaxableIncome());
            model.addAttribute("taxRate", taxCalculation.getTaxRate());
            model.addAttribute("grossTaxAmount", taxCalculation.getGrossTaxAmount());
            model.addAttribute("tdsCredits", taxCalculation.getTdsCredits());
            model.addAttribute("advanceTax", taxCalculation.getAdvanceTax());
            model.addAttribute("netTaxPayable", taxCalculation.getNetTaxPayable());

            return "tax/report";
        } catch (Exception e) {
            logger.error("Error generating tax report: ", e);
            model.addAttribute("error", true);
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
} 