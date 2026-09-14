package com.hospitality.mis.entity.auth;

import com.hospitality.mis.entity.guest.Guest;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_accounts")
/** Tài khoản đăng nhập của khách, liên kết một-một với hồ sơ {@link Guest}. */
public class CustomerAccount {
    /** Khóa kỹ thuật do cơ sở dữ liệu sinh; không dùng làm số điện thoại đăng nhập. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Hồ sơ khách sở hữu duy nhất tài khoản này. */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false, unique = true)
    private Guest guest;
    /** Số điện thoại duy nhất dùng làm tên đăng nhập của khách. */
    @Column(nullable = false, unique = true, length = 15) private String phone;
    /** Mật khẩu đã được băm; entity không lưu mật khẩu dạng rõ. */
    @Column(nullable = false, length = 255) private String password;
    /** Cờ cho phép xác thực; false thì tài khoản bị vô hiệu hóa. */
    @Column(nullable = false) private boolean enabled = true;
    /** Cờ khóa đăng nhập do chính sách bảo mật; false thì không được đăng nhập. */
    @Column(nullable = false) private boolean accountNonLocked = true;

    public Long getId() { return id; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(boolean value) { this.accountNonLocked = value; }
}
