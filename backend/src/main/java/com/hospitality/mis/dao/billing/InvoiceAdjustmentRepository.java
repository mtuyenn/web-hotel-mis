package com.hospitality.mis.dao.billing;

import com.hospitality.mis.entity.billing.InvoiceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceAdjustmentRepository extends JpaRepository<InvoiceAdjustment, Long> {
    List<InvoiceAdjustment> findByIdempotencyKeyStartingWith(String prefix);
}
