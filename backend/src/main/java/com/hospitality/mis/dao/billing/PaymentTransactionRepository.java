package com.hospitality.mis.dao.billing;

import com.hospitality.mis.entity.billing.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hospitality.mis.entity.billing.PaymentTransaction.TransactionStatus;
import com.hospitality.mis.entity.billing.PaymentTransaction.TransactionType;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByInvoiceIdOrderByOccurredAtAsc(Long invoiceId);
    List<PaymentTransaction> findByInvoiceIdAndStatus(Long invoiceId, TransactionStatus status);
    @org.springframework.data.jpa.repository.Query("select p from PaymentTransaction p where p.idempotencyKey like concat(:prefix, '%') order by p.id desc")
    List<PaymentTransaction> findByIdempotencyKeyPrefix(@org.springframework.data.repository.query.Param("prefix") String prefix);
    @org.springframework.data.jpa.repository.Query(value = """
            select coalesce(sum(case when type = 'PAYMENT' then amount else -amount end), 0)
            from payment_transactions
            where actor_id = :actor and method = 'CASH' and status = 'COMPLETED'
              and occurred_at > :fromAt and occurred_at <= :toAt
            """, nativeQuery = true)
    BigDecimal netCashByActorBetween(@org.springframework.data.repository.query.Param("actor") String actor,
                                     @org.springframework.data.repository.query.Param("fromAt") LocalDateTime fromAt,
                                     @org.springframework.data.repository.query.Param("toAt") LocalDateTime toAt);
}
