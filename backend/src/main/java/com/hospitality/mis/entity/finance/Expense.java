package com.hospitality.mis.entity.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "expenses")
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String category;
    @Column(nullable = false, length = 255) private String description;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "paid_by", nullable = false, length = 50) private String paidBy;
    @Column(name = "paid_at", nullable = false) private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ExpenseStatus status = ExpenseStatus.RECORDED;
    public enum ExpenseStatus { RECORDED, APPROVED, VOIDED }
    public Long getId() { return id; } public String getCategory() { return category; } public void setCategory(String v) { category = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public String getPaidBy() { return paidBy; } public void setPaidBy(String v) { paidBy = v; }
    public LocalDateTime getPaidAt() { return paidAt; } public void setPaidAt(LocalDateTime v) { paidAt = v; }
    public ExpenseStatus getStatus() { return status; } public void setStatus(ExpenseStatus v) { status = v; }
}
