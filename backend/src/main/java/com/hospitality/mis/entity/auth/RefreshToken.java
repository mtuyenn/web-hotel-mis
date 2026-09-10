package com.hospitality.mis.entity.auth;

import com.hospitality.mis.service.auth.JwtTokenService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", length = 10)
    private String employeeId;

    @Column(name = "customer_account_id")
    private Long customerAccountId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_hash", length = 64)
    private String replacedByHash;

    protected RefreshToken() {
    }

    private RefreshToken(String employeeId, Long customerAccountId, String tokenHash, String familyId,
                         Instant issuedAt, Instant expiresAt) {
        if ((employeeId == null) == (customerAccountId == null)) {
            throw new IllegalArgumentException("A refresh token must have exactly one principal owner");
        }
        this.employeeId = employeeId;
        this.customerAccountId = customerAccountId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken issue(String employeeId, String tokenHash, String familyId,
                                     Instant issuedAt, Instant expiresAt) {
        return new RefreshToken(employeeId, null, tokenHash, familyId, issuedAt, expiresAt);
    }

    public static RefreshToken issueCustomer(Long customerAccountId, String tokenHash, String familyId,
                                             Instant issuedAt, Instant expiresAt) {
        return new RefreshToken(null, customerAccountId, tokenHash, familyId, issuedAt, expiresAt);
    }

    public String getEmployeeId() { return employeeId; }
    public Long getCustomerAccountId() { return customerAccountId; }
    public String getTokenHash() { return tokenHash; }
    public String getFamilyId() { return familyId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getReplacedByHash() { return replacedByHash; }

    public String getPrincipalType() {
        return principalType().name();
    }

    public String getPrincipalId() {
        return principalType() == JwtTokenService.PrincipalType.EMPLOYEE
                ? employeeId : String.valueOf(customerAccountId);
    }

    public JwtTokenService.PrincipalType principalType() {
        if (employeeId != null && customerAccountId == null) return JwtTokenService.PrincipalType.EMPLOYEE;
        if (employeeId == null && customerAccountId != null) return JwtTokenService.PrincipalType.CUSTOMER;
        throw new IllegalStateException("Refresh token owner is invalid");
    }

    public String auditActor() {
        return principalType() == JwtTokenService.PrincipalType.EMPLOYEE
                ? employeeId : "customer:" + customerAccountId;
    }

    public void revoke(Instant at, String replacementHash) {
        this.revokedAt = at;
        this.replacedByHash = replacementHash;
    }
}
