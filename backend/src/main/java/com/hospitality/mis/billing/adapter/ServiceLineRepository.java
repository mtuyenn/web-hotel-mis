package com.hospitality.mis.billing.adapter;

import com.hospitality.mis.billing.domain.ChiTietDichVu;
import com.hospitality.mis.billing.domain.ChiTietDichVuId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceLineRepository extends JpaRepository<ChiTietDichVu, ChiTietDichVuId> {
}
