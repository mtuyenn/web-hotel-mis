package com.hospitality.mis.service.auth;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.dao.auth.CustomerAccountRepository;
import com.hospitality.mis.dao.guest.GuestStore;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dto.auth.CustomerAccountDtos;
import com.hospitality.mis.dto.guest.GuestDtos;
import com.hospitality.mis.entity.auth.CustomerAccount;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.service.governance.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAccountService {
    private final CustomerAccountRepository accounts;
    private final GuestStore guests;
    private final EmployeeRepository employees;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public CustomerAccountService(CustomerAccountRepository accounts, GuestStore guests,
                                  EmployeeRepository employees, PasswordEncoder passwordEncoder,
                                  AuditService audit) {
        this.accounts = accounts;
        this.guests = guests;
        this.employees = employees;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Transactional
    public CustomerAccountDtos.Response register(CustomerAccountDtos.RegisterRequest request) {
        if (request == null) throw new DomainException("INVALID_CUSTOMER_REQUEST", "Thiếu nội dung đăng ký");
        String phone = request.phone().trim();
        String identityNumber = request.identityNumber().trim();
        if (accounts.existsByPhone(phone) || employees.existsByPhone(phone)) {
            throw new DomainException("PHONE_ALREADY_IN_USE", "Số điện thoại đã được sử dụng");
        }
        if (request.password() == null || request.password().length() < 8 || request.password().length() > 72) {
            throw new DomainException("PASSWORD_INVALID", "Mật khẩu phải có từ 8 đến 72 ký tự");
        }
        Guest savedGuest = guests.findByPhone(phone).map(existing -> {
            if (!identityNumber.equals(existing.getIdentityNumber())) {
                throw new DomainException("GUEST_IDENTITY_MISMATCH", "Thông tin giấy tờ không khớp hồ sơ khách hàng");
            }
            return existing;
        }).orElseGet(() -> {
            if (guests.findByIdentityNumber(identityNumber).isPresent()) {
                throw new DomainException("GUEST_IDENTITY_EXISTS", "Số giấy tờ khách hàng đã tồn tại");
            }
            Guest guest = guests.newGuest();
            guest.setFullName(request.fullName().trim());
            guest.setIdentityNumber(identityNumber);
            guest.setPhone(phone);
            return guests.save(guest);
        });
        CustomerAccount account = new CustomerAccount();
        account.setGuest(savedGuest);
        account.setPhone(phone);
        account.setPassword(passwordEncoder.encode(request.password()));
        CustomerAccount saved = accounts.save(account);
        audit.record("SYSTEM", "CUSTOMER_REGISTERED", "CUSTOMER_ACCOUNT", String.valueOf(saved.getId()),
                null, null, null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerAccountDtos.MeResponse me(Long customerAccountId) {
        CustomerAccount account = accounts.findById(customerAccountId)
                .orElseThrow(() -> new DomainException("CUSTOMER_ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản khách hàng"));
        Guest guest = account.getGuest();
        GuestDtos.Response guestResponse = new GuestDtos.Response(guest.getId(), guest.getFullName(), guest.getBirthYear(),
                guest.getIdentityNumber(), guest.getPhone(), guest.getEmail(), guest.getAddress(), guest.getMembershipTier(),
                guest.getTotalSpend(), guest.getLateCancellationCount(), guest.getLateCheckoutCount(), guest.isBookingBlocked());
        return new CustomerAccountDtos.MeResponse(toResponse(account), guestResponse);
    }

    public CustomerAccountDtos.Response toResponse(CustomerAccount account) {
        return new CustomerAccountDtos.Response(account.getId(), account.getGuest().getId(), account.getPhone(),
                account.isEnabled(), account.isAccountNonLocked());
    }
}
