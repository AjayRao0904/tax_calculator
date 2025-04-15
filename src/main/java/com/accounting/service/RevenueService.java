package com.accounting.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accounting.entity.Company;
import com.accounting.entity.Revenue;
import com.accounting.entity.User;
import com.accounting.repository.RevenueRepository;

@Service
@Transactional
public class RevenueService {
    private final RevenueRepository revenueRepository;
    private final CompanyService companyService;

    public RevenueService(RevenueRepository revenueRepository, CompanyService companyService) {
        this.revenueRepository = revenueRepository;
        this.companyService = companyService;
    }

    public List<Revenue> getAllRevenues(User user) {
        return revenueRepository.findByCompanyUser(user);
    }

    public Revenue getRevenue(Long id, User user) {
        return revenueRepository.findById(id)
            .filter(revenue -> revenue.getCompany().getUser().equals(user))
            .orElseThrow(() -> new RuntimeException("Revenue not found"));
    }

    public Revenue createRevenue(Revenue revenue, Long companyId, User user) {
        Company company = companyService.getCompany(companyId, user);
        revenue.setCompany(company);
        return revenueRepository.save(revenue);
    }

    public Revenue updateRevenue(Revenue revenue, Long companyId, User user) {
        getRevenue(revenue.getId(), user); // Verify ownership
        Company company = companyService.getCompany(companyId, user);
        revenue.setCompany(company);
        return revenueRepository.save(revenue);
    }

    public void deleteRevenue(Long id, User user) {
        Revenue revenue = getRevenue(id, user);
        revenueRepository.delete(revenue);
    }

    public List<Revenue> getCompanyRevenues(Long companyId, User user) {
        Company company = companyService.getCompany(companyId, user);
        return revenueRepository.findByCompany(company);
    }

    public List<Revenue> getCompanyRevenuesByDateRange(Long companyId, LocalDate startDate, LocalDate endDate, User user) {
        Company company = companyService.getCompany(companyId, user);
        return revenueRepository.findByCompanyAndDateBetween(company, startDate, endDate);
    }

    public BigDecimal getTotalRevenue(Long companyId, User user) {
        List<Revenue> revenues = getCompanyRevenues(companyId, user);
        return revenues.stream()
            .map(Revenue::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 