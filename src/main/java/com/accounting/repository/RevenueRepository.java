package com.accounting.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accounting.entity.Company;
import com.accounting.entity.Revenue;
import com.accounting.entity.User;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    List<Revenue> findByCompany(Company company);
    List<Revenue> findByCompanyAndDateBetween(Company company, LocalDate startDate, LocalDate endDate);
    List<Revenue> findByCompanyUser(User user);
} 