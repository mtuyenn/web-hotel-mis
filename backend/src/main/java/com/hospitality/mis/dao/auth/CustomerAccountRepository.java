package com.hospitality.mis.dao.auth;

import com.hospitality.mis.entity.auth.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    Optional<CustomerAccount> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
