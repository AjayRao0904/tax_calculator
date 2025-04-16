package com.accounting.entity;

import java.time.Year;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "company_profile")
public class CompanyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Year incorporationYear;

    @Column
    private Year startManufacturingYear;

    @Column(nullable = false)
    private boolean usesOldMachinery = false;

    @Column(nullable = false)
    private boolean inRestrictedBusiness = false;

    @Column(length = 500)
    private String businessDescription;

    public boolean isNewManufacturingCompany() {
        return incorporationYear.getValue() >= 2019 &&
               startManufacturingYear != null &&
               startManufacturingYear.getValue() <= 2024 &&
               !usesOldMachinery &&
               !inRestrictedBusiness;
    }
} 