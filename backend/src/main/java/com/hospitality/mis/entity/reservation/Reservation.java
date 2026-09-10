package com.hospitality.mis.entity.reservation;

import com.hospitality.mis.entity.billing.ServiceUsage;
import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.identity.Employee;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Canonical owner of the reservations aggregate and the {@code reservations} table. */
@Entity
@Table(name = "reservations")
@Access(AccessType.FIELD)
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "booked_at", nullable = false) private LocalDateTime bookedAt = LocalDateTime.now();
    @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2) private BigDecimal depositAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 30) private ReservationStatus status = ReservationStatus.DRAFT;
    @Column(name = "rental_type", nullable = false, length = 20) private String rentalType = "PACKAGE";
    @Column(name = "actual_check_in") private LocalDateTime actualCheckIn;
    @Column(name = "actual_check_out") private LocalDateTime actualCheckOut;
    @Column(name = "extension_minutes", nullable = false) private int extensionMinutes;
    @Column(name = "idempotency_key", length = 100, unique = true) private String idempotencyKey;
    @Version @Column(name = "version", nullable = false) private long version;

    /** Cancellation details are carried by the command audit when the current
     * schema cannot be migrated as part of this slice. */
    @Transient private String cancellationReason;
    @Transient private CancellationOutcome cancellationOutcome;
    @Transient private String canonicalRequestFingerprint;

    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationRoom> rooms = new ArrayList<>();
    @OneToMany(mappedBy = "reservation", fetch = FetchType.LAZY) private Set<ServiceUsage> serviceUsages;
    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true) private Invoice invoice;

    public Reservation() {}
    public Long getId() { return id; }
    public Guest getGuest() { return guest; }
    public Employee getEmployee() { return employee; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public ReservationStatus getStatus() { return status; }
    public String getRentalType() { return rentalType; }
    public LocalDateTime getActualCheckIn() { return actualCheckIn; }
    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public int getExtensionMinutes() { return extensionMinutes; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getVersion() { return version; }
    public String getCancellationReason() { return cancellationReason; }
    public CancellationOutcome getCancellationOutcome() { return cancellationOutcome; }
    public String getCanonicalRequestFingerprint() { return canonicalRequestFingerprint; }
    public List<ReservationRoom> getRooms() { return rooms; }
    public Set<ServiceUsage> getServiceUsages() { return serviceUsages; }
    public Invoice getInvoice() { return invoice; }
    public void setGuest(Guest value) { guest = value; }
    public void setEmployee(Employee value) { employee = value; }
    public void setDepositAmount(BigDecimal value) { depositAmount = value; }
    public void setRentalType(String value) { rentalType = value; }
    public void setIdempotencyKey(String value) { idempotencyKey = value; }
    public void setCancellationReason(String value) { cancellationReason = value; }
    public void setCancellationOutcome(CancellationOutcome value) { cancellationOutcome = value; }
    public void setCanonicalRequestFingerprint(String value) { canonicalRequestFingerprint = value; }
    public void setActualCheckIn(LocalDateTime value) { actualCheckIn = value; }
    public void setActualCheckOut(LocalDateTime value) { actualCheckOut = value; }
    public void setExtensionMinutes(int value) { extensionMinutes = value; }
    public void addRoom(ReservationRoom room) { room.setReservation(this); rooms.add(room); }
    public void transitionTo(ReservationStatus next) {
        if (!status.canTransitionTo(next)) throw new IllegalStateException("Invalid reservation transition: " + status + " -> " + next);
        status = next;
    }
}
