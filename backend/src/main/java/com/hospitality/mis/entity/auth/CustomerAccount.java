package com.hospitality.mis.entity.auth;

import com.hospitality.mis.entity.guest.Guest;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_accounts")
public class CustomerAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false, unique = true)
    private Guest guest;
    @Column(nullable = false, unique = true, length = 15) private String phone;
    @Column(nullable = false, length = 255) private String password;
    @Column(nullable = false) private boolean enabled = true;
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
