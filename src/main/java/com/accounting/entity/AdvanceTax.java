package com.accounting.entity;

import java.math.BigDecimal;
import java.time.Month;
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
@Table(name = "advance_tax")
public class AdvanceTax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Year financialYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Quarter quarter;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal percentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public enum Quarter {
        Q1(Month.JUNE, 15),
        Q2(Month.SEPTEMBER, 45),
        Q3(Month.DECEMBER, 75),
        Q4(Month.MARCH, 100);

        private final Month month;
        private final int percentage;

        Quarter(Month month, int percentage) {
            this.month = month;
            this.percentage = percentage;
        }

        public Month getMonth() {
            return month;
        }

        public int getPercentage() {
            return percentage;
        }
    }
} 