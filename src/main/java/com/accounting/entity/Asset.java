package com.accounting.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory category;

    @Column(nullable = false)
    private BigDecimal cost;

    @Column(nullable = false)
    private boolean isNew;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private BigDecimal depreciationRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public enum AssetCategory {
        BUILDING,
        PLANT_MACHINERY,
        COMPUTERS,
        FURNITURE,
        VEHICLES,
        OTHER
    }

    public BigDecimal calculateDepreciation() {
        return cost.multiply(depreciationRate.divide(BigDecimal.valueOf(100)));
    }

    public BigDecimal calculateAdditionalDepreciation() {
        if (isNew && category == AssetCategory.PLANT_MACHINERY) {
            return cost.multiply(BigDecimal.valueOf(0.20));
        }
        return BigDecimal.ZERO;
    }
} 