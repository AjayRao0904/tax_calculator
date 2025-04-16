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
import com.accounting.exception.ResourceNotFoundException;

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

    @Transactional
    public Expense createExpense(Expense expense) {
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        
        // Validate required fields
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (expense.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (expense.getPaymentMode() == null) {
            throw new IllegalArgumentException("Payment mode is required");
        }
        if (expense.getCompany() == null) {
            throw new IllegalArgumentException("Company is required");
        }
        
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Expense expense, User user) {
        // Verify ownership
        Expense existingExpense = getExpense(expense.getId(), user);
        
        // Update fields
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setPaymentMode(expense.getPaymentMode());
        existingExpense.setDate(expense.getDate());
        existingExpense.setDescription(expense.getDescription());
        existingExpense.setCompany(expense.getCompany());
        
        return expenseRepository.save(existingExpense);
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

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByCompanyUser(user);
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    public void saveExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
} 