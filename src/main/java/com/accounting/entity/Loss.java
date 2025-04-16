package com.accounting.entity;

import java.math.BigDecimal;
import java.time.Year;

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
@Table(name = "losses")
public class Loss {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LossType type;

    @Column(nullable = false)
    private Year year;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal utilizedAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public enum LossType {
        BUSINESS_LOSS,
        SPECULATIVE_LOSS,
        CAPITAL_LOSS,
        HOUSE_PROPERTY_LOSS
    }

    public boolean isExpired(int currentYear) {
        int yearsElapsed = currentYear - year.getValue();
        return (type == LossType.BUSINESS_LOSS && yearsElapsed > 8) ||
               (type == LossType.SPECULATIVE_LOSS && yearsElapsed > 4);
    }

    public BigDecimal getUnutilizedAmount() {
        return amount.subtract(utilizedAmount);
    }
} 