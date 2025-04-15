package com.accounting.controller;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

import com.accounting.entity.Company;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.ExpenseService;
import com.accounting.service.RevenueService;
import com.accounting.service.UserService;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Controller
@RequestMapping("/tax-calculator")
public class TaxCalculatorController {
    private final CompanyService companyService;
    private final RevenueService revenueService;
    private final ExpenseService expenseService;
    private final UserService userService;

    public TaxCalculatorController(CompanyService companyService, RevenueService revenueService,
            ExpenseService expenseService, UserService userService) {
        this.companyService = companyService;
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public String showCalculator(Model model) {
        User user = getCurrentUser();
        List<Company> companies = companyService.getUserCompanies(user);
        model.addAttribute("companies", companies);
        model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("endDate", LocalDate.now());
        return "tax-calculator/calculator";
    }

    @PostMapping("/calculate")
    public String calculateTax(@RequestParam Long companyId, 
                             @RequestParam BigDecimal taxRate,
                             @RequestParam LocalDate startDate,
                             @RequestParam LocalDate endDate,
                             Model model) {
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

        model.addAttribute("company", company);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("taxableIncome", taxableIncome);
        model.addAttribute("taxRate", taxRate);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("companies", companyService.getUserCompanies(user));
        
        return "tax-calculator/calculator";
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

            // Create PDF
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tax-report.pdf");

            return ResponseEntity
                .ok()
                .headers(headers)
                .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 