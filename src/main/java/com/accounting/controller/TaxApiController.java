package com.accounting.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.accounting.entity.Company;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.CorporateTaxCalculatorService;
import com.accounting.service.ExpenseService;
import com.accounting.service.RevenueService;
import com.accounting.service.UserService;

@RestController
@RequestMapping("/api/tax")
public class TaxApiController {
    private final CompanyService companyService;
    private final RevenueService revenueService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final CorporateTaxCalculatorService taxCalculatorService;

    public TaxApiController(CompanyService companyService, 
                          RevenueService revenueService,
                          ExpenseService expenseService, 
                          UserService userService,
                          CorporateTaxCalculatorService taxCalculatorService) {
        this.companyService = companyService;
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.taxCalculatorService = taxCalculatorService;
    }

    @GetMapping("/report")
    public ResponseEntity<?> getTaxReport(
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "20") BigDecimal taxRate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            User user = getCurrentUser();
            Company company = companyService.getCompany(companyId, user);
            
            // Set default dates if not provided
            LocalDate finalStartDate = startDate != null ? startDate : LocalDate.now().withDayOfYear(1);
            LocalDate finalEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Calculate total revenue
            BigDecimal totalRevenue = revenueService.getCompanyRevenuesByDateRange(companyId, finalStartDate, finalEndDate, user)
                .stream()
                .map(revenue -> revenue.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total expenses
            BigDecimal totalExpenses = expenseService.getCompanyExpensesByDateRange(companyId, finalStartDate, finalEndDate, user)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate taxable income
            BigDecimal taxableIncome = totalRevenue.subtract(totalExpenses);
            
            // Calculate tax amount
            BigDecimal taxAmount = taxableIncome.multiply(taxRate.divide(BigDecimal.valueOf(100)));

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("company", company);
            response.put("startDate", finalStartDate);
            response.put("endDate", finalEndDate);
            response.put("totalRevenue", totalRevenue);
            response.put("totalExpenses", totalExpenses);
            response.put("taxableIncome", taxableIncome);
            response.put("taxRate", taxRate);
            response.put("taxAmount", taxAmount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 