package com.accounting.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.accounting.entity.Company;
import com.accounting.entity.Expense;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.ExpenseService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;
    private final CompanyService companyService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, CompanyService companyService, UserService userService) {
        this.expenseService = expenseService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping
    public String listExpenses(Model model) {
        User user = getCurrentUser();
        List<Company> companies = companyService.getUserCompanies(user);
        model.addAttribute("companies", companies);
        model.addAttribute("expenses", expenseService.getAllExpenses(user));
        return "expenses/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("expense", new Expense());
        model.addAttribute("companies", companyService.getUserCompanies(user));
        return "expenses/form";
    }

    @PostMapping
    public String createExpense(@ModelAttribute Expense expense, @RequestParam Long companyId) {
        User user = getCurrentUser();
        Company company = companyService.getCompany(companyId, user);
        expense.setCompany(company);
        expenseService.createExpense(expense);
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Expense expense = expenseService.getExpense(id, user);
        model.addAttribute("expense", expense);
        model.addAttribute("companies", companyService.getUserCompanies(user));
        return "expenses/form";
    }

    @PostMapping("/{id}")
    public String updateExpense(@PathVariable Long id, @ModelAttribute Expense expense, 
                              @RequestParam Long companyId) {
        User user = getCurrentUser();
        Company company = companyService.getCompany(companyId, user);
        expense.setId(id);
        expense.setCompany(company);
        expenseService.updateExpense(expense, user);
        return "redirect:/expenses";
    }

    @DeleteMapping("/{id}")
    public String deleteExpense(@PathVariable Long id) {
        User user = getCurrentUser();
        expenseService.deleteExpense(id, user);
        return "redirect:/expenses";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 