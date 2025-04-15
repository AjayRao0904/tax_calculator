package com.accounting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accounting.entity.Company;
import com.accounting.entity.User;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByUser(User user);
    List<Company> findByUserAndNameContaining(User user, String name);
} 