package com.hospitality.mis.identity.domain;

import com.hospitality.mis.reservation.domain.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Identity-owned employee/account model and JPA mapping.
 *
 * <p>This is the single concrete JPA owner for the canonical {@code employees}
 * account aggregate. Authentication status and lockout state are persisted on
 * the same aggregate.</p>
 */
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @Column(name = "id", length = 10, nullable = false)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 30)
    private EmployeeRole role;

    @Column(name = "address", length = 255, columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "phone", length = 15, unique = true, nullable = false)
    private String phone;

    /** Matches Reservation.employee; the schema has a restrictive FK, so no cascade. */
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public Employee() {
    }

    protected Employee(String employeeId, String fullName, String password,
                       EmployeeRole role, String address, String phone) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.address = address;
        this.phone = phone;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "last_failed_login_at")
    private java.time.Instant lastFailedLoginAt;

    @Column(name = "last_login_at")
    private java.time.Instant lastLoginAt;

    @Transient
    public Set<Permission> getPermissions() {
        return role.permissions();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonLocked(boolean value) { this.accountNonLocked = value; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public java.time.Instant getLastFailedLoginAt() { return lastFailedLoginAt; }
    public java.time.Instant getLastLoginAt() { return lastLoginAt; }

    public void recordLoginFailure(java.time.Instant at, int maxAttempts) {
        failedLoginAttempts++;
        lastFailedLoginAt = at;
        if (failedLoginAttempts >= maxAttempts) accountNonLocked = false;
    }

    public void recordLoginSuccess(java.time.Instant at) {
        failedLoginAttempts = 0;
        lastLoginAt = at;
    }

    public void unlockAfterPasswordReset() {
        failedLoginAttempts = 0;
        accountNonLocked = true;
    }
}
