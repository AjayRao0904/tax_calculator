package com.accounting.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        User currentUser = getCurrentUser();
        List<Expense> expenses = expenseService.getExpensesByUser(currentUser);
        List<Company> companies = companyService.getUserCompanies(currentUser);
        
        BigDecimal totalAmount = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal taxDeductibleAmount = expenses.stream()
            .filter(Expense::isTaxDeductible)
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal nonDeductibleAmount = expenses.stream()
            .filter(e -> !e.isTaxDeductible())
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("expenses", expenses);
        model.addAttribute("companies", companies);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("taxDeductibleAmount", taxDeductibleAmount);
        model.addAttribute("nonDeductibleAmount", nonDeductibleAmount);
        model.addAttribute("expenseCategories", Expense.ExpenseCategory.values());
        model.addAttribute("paymentModes", Expense.PaymentMode.values());
        
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
        try {
            User user = getCurrentUser();
            Company company = companyService.getCompany(companyId, user);
            expense.setCompany(company);
            
            // Ensure the date is set if not provided
            if (expense.getDate() == null) {
                expense.setDate(LocalDate.now());
            }
            
            // Set default payment reference if not provided
            if (expense.getPaymentReference() == null || expense.getPaymentReference().trim().isEmpty()) {
                expense.setPaymentReference("N/A");
            }
            
            expenseService.createExpense(expense);
            return "redirect:/expenses";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Add error message to flash attributes
            return "redirect:/expenses?error=true&message=" + e.getMessage();
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Expense expense = expenseService.getExpense(id, user);
        model.addAttribute("expense", expense);
        model.addAttribute("companies", companyService.getUserCompanies(user));
        model.addAttribute("categories", Expense.ExpenseCategory.values());
        model.addAttribute("paymentModes", Expense.PaymentMode.values());
        return "expenses/form";
    }

    @PostMapping("/{id}")
    public String updateExpense(@PathVariable Long id, @ModelAttribute Expense expense, 
                              @RequestParam Long companyId) {
        User user = getCurrentUser();
        Company company = companyService.getCompany(companyId, user);
        expense.setId(id);
        expense.setCompany(company);
        
        // Ensure the date is set if not provided
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        
        expenseService.updateExpense(expense, user);
        return "redirect:/expenses";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseService.getExpense(id);
        
        if (expense == null || !expense.getCompany().getUser().equals(currentUser)) {
            return ResponseEntity.notFound().build();
        }
        
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        model.addAttribute("error", true);
        model.addAttribute("message", e.getMessage());
        return listExpenses(model);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 