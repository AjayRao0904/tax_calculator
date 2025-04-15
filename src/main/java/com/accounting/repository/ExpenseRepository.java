package com.accounting.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.accounting.entity.Company;
import com.accounting.entity.Expense;
import com.accounting.entity.User;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCompany(Company company);
    List<Expense> findByCompanyUser(User user);
    List<Expense> findByCompanyAndDateBetween(Company company, LocalDate startDate, LocalDate endDate);
    List<Expense> findByCompanyAndCategory(Company company, String category);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.company = ?1")
    BigDecimal sumByCompany(Company company);
} 