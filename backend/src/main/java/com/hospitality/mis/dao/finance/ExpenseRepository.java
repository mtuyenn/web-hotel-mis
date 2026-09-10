package com.hospitality.mis.dao.finance;

import com.hospitality.mis.entity.finance.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByStatusOrderByPaidAtDesc(Expense.ExpenseStatus status);
    List<Expense> findAllByOrderByPaidAtDesc();
}
