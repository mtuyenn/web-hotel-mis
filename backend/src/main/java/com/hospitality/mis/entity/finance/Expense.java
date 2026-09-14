package com.hospitality.mis.entity.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Khoản chi đã phát sinh trong hoạt động khách sạn. */
@Entity @Table(name = "expenses")
public class Expense {
    /** ID khoản chi do cơ sở dữ liệu sinh. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String category;
    @Column(nullable = false, length = 255) private String description;
    /** Giá trị tiền tệ của khoản chi. */
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "paid_by", nullable = false, length = 50) private String paidBy;
    @Column(name = "paid_at", nullable = false) private LocalDateTime paidAt;
    /** Vòng đời phê duyệt/hủy của khoản chi. */
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ExpenseStatus status = ExpenseStatus.RECORDED;
    /** Các trạng thái nghiệp vụ của khoản chi. */
    public enum ExpenseStatus {
        /** Đã ghi nhận nhưng chưa phê duyệt. */
        RECORDED,
        /** Đã được phê duyệt để tính vào sổ chi. */
        APPROVED,
        /** Bị hủy, không còn hiệu lực quyết toán. */
        VOIDED
    }
    public Long getId() { return id; } public String getCategory() { return category; } public void setCategory(String v) { category = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount = v; }
    public String getPaidBy() { return paidBy; } public void setPaidBy(String v) { paidBy = v; }
    public LocalDateTime getPaidAt() { return paidAt; } public void setPaidAt(LocalDateTime v) { paidAt = v; }
    public ExpenseStatus getStatus() { return status; } public void setStatus(ExpenseStatus v) { status = v; }
}
