package com.accounting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accounting.entity.Company;
import com.accounting.entity.Expense;
import com.accounting.entity.User;
import com.accounting.repository.ExpenseRepository;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CompanyService companyService;

    public ExpenseService(ExpenseRepository expenseRepository, CompanyService companyService) {
        this.expenseRepository = expenseRepository;
        this.companyService = companyService;
    }

    public List<Expense> getAllExpenses(User user) {
        return expenseRepository.findByCompanyUser(user);
    }

    public List<Expense> getCompanyExpenses(Long companyId, User user) {
        Company company = companyService.getCompany(companyId, user);
        return expenseRepository.findByCompany(company);
    }

    public Expense getExpense(Long id, User user) {
        return expenseRepository.findById(id)
            .filter(expense -> expense.getCompany().getUser().equals(user))
            .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Expense expense, User user) {
        getExpense(expense.getId(), user); // Verify ownership
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id, User user) {
        Expense expense = getExpense(id, user);
        expenseRepository.delete(expense);
    }

    public List<Expense> getCompanyExpensesByDateRange(Long companyId, LocalDate startDate, LocalDate endDate, User user) {
        Company company = companyService.getCompany(companyId, user);
        return expenseRepository.findByCompanyAndDateBetween(company, startDate, endDate);
    }

    public List<Expense> getCompanyExpensesByCategory(Long companyId, String category, User user) {
        Company company = companyService.getCompany(companyId, user);
        return expenseRepository.findByCompanyAndCategory(company, category);
    }

    public BigDecimal getTotalExpenses(Long companyId, User user) {
        Company company = companyService.getCompany(companyId, user);
        return expenseRepository.sumByCompany(company);
    }
} 