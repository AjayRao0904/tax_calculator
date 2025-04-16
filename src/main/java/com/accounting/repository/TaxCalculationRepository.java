package com.accounting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.accounting.entity.TaxCalculation;
import com.accounting.entity.Company;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {
    List<TaxCalculation> findByCompanyOrderByCalculationDateDesc(Company company);
    List<TaxCalculation> findByCompanyAndStartDateBetweenOrderByCalculationDateDesc(
        Company company, LocalDate startDate, LocalDate endDate);
} 