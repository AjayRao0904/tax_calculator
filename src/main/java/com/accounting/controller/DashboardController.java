package com.accounting.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.accounting.entity.Company;
import com.accounting.entity.Expense;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.ExpenseService;
import com.accounting.service.RevenueService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final CompanyService companyService;
    private final RevenueService revenueService;
    private final ExpenseService expenseService;
    private final UserService userService;

    public DashboardController(CompanyService companyService, RevenueService revenueService, ExpenseService expenseService, UserService userService) {
        this.companyService = companyService;
        this.revenueService = revenueService;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    @GetMapping
    public String showDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        
        List<Company> companies = companyService.getUserCompanies(user);
        Map<Long, BigDecimal> companyRevenues = new HashMap<>();
        Map<Long, BigDecimal> companyExpenses = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Company company : companies) {
            BigDecimal revenue = revenueService.getTotalRevenue(company.getId(), user);
            List<Expense> expenses = expenseService.getCompanyExpenses(company.getId(), user);
            BigDecimal expense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            companyRevenues.put(company.getId(), revenue);
            companyExpenses.put(company.getId(), expense);
            
            totalRevenue = totalRevenue.add(revenue);
            totalExpense = totalExpense.add(expense);
        }

        model.addAttribute("companies", companies);
        model.addAttribute("companyRevenues", companyRevenues);
        model.addAttribute("companyExpenses", companyExpenses);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("newCompany", new Company());
        
        return "dashboard";
    }
} 