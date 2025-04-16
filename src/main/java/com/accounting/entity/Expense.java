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
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "payment_reference", columnDefinition = "varchar(255) default 'N/A'")
    private String paymentReference;

    public enum ExpenseCategory {
        RENT("Rent", true),
        SALARIES("Salaries", true),
        UTILITIES("Utilities", true),
        OFFICE_SUPPLIES("Office Supplies", true),
        TRAVEL("Travel", true),
        ENTERTAINMENT("Entertainment", false),
        PENALTY_FINES("Penalties & Fines", false),
        DONATIONS_CSR("Donations & CSR", false),
        CLUB_MEMBERSHIPS("Club Memberships", false),
        INTEREST_PAID("Interest Paid", true),
        POLITICAL_CONTRIBUTIONS("Political Contributions", false),
        CAPITAL_EXPENDITURE("Capital Expenditure", false),
        OTHER("Other", true);

        private final String displayName;
        private final boolean taxDeductible;

        ExpenseCategory(String displayName, boolean taxDeductible) {
            this.displayName = displayName;
            this.taxDeductible = taxDeductible;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isTaxDeductible() {
            return taxDeductible;
        }
    }

    public enum PaymentMode {
        CASH,
        BANK_TRANSFER,
        CHEQUE,
        CREDIT_CARD,
        OTHER
    }

    public boolean isTaxDeductible() {
        if (category == null) return false;
        
        // Check if the expense category is tax deductible
        if (!category.isTaxDeductible()) return false;
        
        // Check for cash payment limit
        if (paymentMode == PaymentMode.CASH && amount.compareTo(new BigDecimal("10000")) > 0) {
            return false;
        }
        
        return true;
    }
} 