package com.hospitality.mis.dao.billing;



import com.hospitality.mis.entity.billing.ServiceUsage;

import com.hospitality.mis.entity.billing.ServiceUsageId;

import org.springframework.data.jpa.repository.JpaRepository;



public interface ServiceLineRepository extends JpaRepository<ServiceUsage, ServiceUsageId> {

}
