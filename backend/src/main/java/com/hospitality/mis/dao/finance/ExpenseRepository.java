package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Kho các khoản chi vận hành và truy vấn theo trạng thái thanh toán. */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    /** Lấy các khoản chi theo trạng thái, sắp xếp khoản thanh toán mới nhất trước. */
    List<Expense> findByStatusOrderByPaidAtDesc(Expense.ExpenseStatus status);

    /** Lấy toàn bộ khoản chi theo thời điểm thanh toán giảm dần. */
    List<Expense> findAllByOrderByPaidAtDesc();
}
