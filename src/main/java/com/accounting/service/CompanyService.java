package com.accounting.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accounting.entity.Company;
import com.accounting.entity.User;
import com.accounting.repository.CompanyRepository;

@Service
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getUserCompanies(User user) {
        return companyRepository.findByUser(user);
    }

    public Company getCompany(Long id, User user) {
        return companyRepository.findById(id)
            .filter(company -> company.getUser().equals(user))
            .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Company createCompany(Company company, User user) {
        company.setUser(user);
        return companyRepository.save(company);
    }

    public Company updateCompany(Company company, User user) {
        Company existingCompany = getCompany(company.getId(), user);
        existingCompany.setName(company.getName());
        existingCompany.setAddress(company.getAddress());
        existingCompany.setTaxNumber(company.getTaxNumber());
        return companyRepository.save(existingCompany);
    }

    public void deleteCompany(Long id, User user) {
        Company company = getCompany(id, user);
        companyRepository.delete(company);
    }

    // Alias for getUserCompanies to maintain compatibility
    public List<Company> getCompaniesByUser(User user) {
        return getUserCompanies(user);
    }
} 