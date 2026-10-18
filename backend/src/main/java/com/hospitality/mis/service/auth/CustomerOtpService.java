package com.hospitality.mis.service.auth;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.auth.CustomerAccountDatabase;
import org.springframework.stereotype.Service;

/** Owns customer-account checks needed by the OTP use cases. */
@Service
public class CustomerOtpService {
    private final CustomerAccountDatabase customerAccounts;

    public CustomerOtpService(CustomerAccountDatabase customerAccounts) {
        this.customerAccounts = customerAccounts;
    }

    public void requireEmailAvailable(String email) {
        if (customerAccounts.email(email).isPresent()) throw new DomainException("EMAIL_ALREADY_IN_USE", "Địa chỉ email này đã được sử dụng cho một tài khoản khác.");
    }

    public void requireAccountEmail(String email) {
        if (customerAccounts.email(email).isEmpty()) throw new DomainException("ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản khách hàng nào liên kết với email này.");
    }
}
