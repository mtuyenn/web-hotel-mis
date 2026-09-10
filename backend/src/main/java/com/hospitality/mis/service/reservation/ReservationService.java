package com.hospitality.mis.service.reservation;
import com.hospitality.mis.dto.billing.InvoiceDtos;

import com.hospitality.mis.entity.reservation.Reservation;

import com.hospitality.mis.entity.reservation.ReservationRoom;

import com.hospitality.mis.entity.reservation.ReservationStatus;


import com.hospitality.mis.service.billing.BillingService;
import com.hospitality.mis.dao.billing.ServiceLineRepository;
import com.hospitality.mis.dao.billing.ServiceRepository;
import com.hospitality.mis.entity.billing.ServiceUsage;
import com.hospitality.mis.entity.billing.ServiceUsageId;
import com.hospitality.mis.entity.billing.Service;
import com.hospitality.mis.dao.operations.InventoryMovementRepository;
import com.hospitality.mis.entity.operations.InventoryMovement;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.service.governance.AuditService;
import com.hospitality.mis.dao.guest.GuestStore;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.entity.guest.Guest;
import com.hospitality.mis.entity.guest.BookingPolicy;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dto.reservation.ReservationDtos;
import com.hospitality.mis.entity.reservation.*;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import com.hospitality.mis.middleware.security.SecurityActor;
import com.hospitality.mis.middleware.security.ReservationAccess;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.*;

@org.springframework.stereotype.Service
public class ReservationService {
    private static final List<ReservationStatus> IGNORED = List.of(ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW, ReservationStatus.CHECKED_OUT);
    private final ReservationRepository reservations; private final GuestStore sharedGuests; private final EmployeeRepository employees;
    private final RoomRepository rooms; private final AuditService audit; private final BillingService billing;
    private final ServiceRepository serviceCatalog; private final ServiceLineRepository serviceLines;
    private final InventoryMovementRepository inventoryMovements;
    private final IdempotencySupport idempotency = new IdempotencySupport();
    private final BookingPolicy bookingPolicy = BookingPolicy.defaults();
    private Clock clock = Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @Value("${hotel.policy.late-cancellation-hours:${HOTEL_LATE_CANCELLATION_HOURS:48}}")
    private long lateCancellationHours = 48;
    public ReservationService(ReservationRepository reservations, GuestStore sharedGuests, EmployeeRepository employees, RoomRepository rooms,
                              AuditService audit, BillingService billing, ServiceRepository serviceCatalog, ServiceLineRepository serviceLines,
                              InventoryMovementRepository inventoryMovements) {
        this.reservations=reservations; this.sharedGuests=sharedGuests; this.employees=employees; this.rooms=rooms; this.audit=audit; this.billing=billing; this.serviceCatalog=serviceCatalog; this.serviceLines=serviceLines;
        this.inventoryMovements = inventoryMovements;
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ReservationDtos.Response create(ReservationDtos.CreateRequest request, String actor) {
        return create(request, actor, request == null ? null : request.idempotencyKey());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ReservationDtos.Response create(ReservationDtos.CreateRequest request, String suppliedActor,
                                           String headerKey) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        if (request == null) throw error("INVALID_REQUEST", "Thiếu nội dung đặt phòng");
        requireActor(actor, request.employeeId());
        String key = effectiveKey(request.idempotencyKey(), headerKey);
        String fingerprint = createFingerprint(request);
        // A locking lookup for a missing unique key takes an InnoDB gap lock and
        // can deadlock two otherwise independent create requests before they
        // reach the canonical room lock. Read first, then serialize on rooms.
        var prior = reservations.findByIdempotencyKey(key);
        if (prior.isPresent()) {
            Reservation existing = prior.get();
            if (!actor.equals(existing.getEmployee().getEmployeeId())
                    || !fingerprint.equals(createFingerprint(existing))) {
                throw error("IDEMPOTENCY_KEY_CONFLICT", "Idempotency key đã được dùng cho yêu cầu khác");
            }
            return toResponse(existing);
        }
        if (request.rooms().size() > 3) throw error("ROOM_LIMIT_EXCEEDED", "Mỗi lượt lưu trú chỉ được đặt tối đa 3 phòng");
        Guest guest = sharedGuest(request.guestId());
        if (!bookingPolicy.allows(guest)) throw error("GUEST_BOOKING_BLOCKED", "Khách hàng đã bị khóa quyền đặt trước");
        Employee employee = employees.findById(request.employeeId()).orElseThrow(() -> error("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
        Set<String> ids = new HashSet<>();
        for (var line : request.rooms()) {
            if (!line.expectedCheckIn().isBefore(line.expectedCheckOut())) throw error("INVALID_INTERVAL", "Thời gian nhận phải trước thời gian trả");
            if (!ids.add(line.roomId())) throw error("DUPLICATE_ROOM", "Không được lặp phòng trong một đơn đặt");
        }
        Map<String, Room> locked = lockRooms(ids);
        prior = reservations.findByIdempotencyKey(key);
        if (prior.isPresent()) {
            Reservation existing = prior.get();
            if (!actor.equals(existing.getEmployee().getEmployeeId())
                    || !fingerprint.equals(createFingerprint(existing))) {
                throw error("IDEMPOTENCY_KEY_CONFLICT", "Idempotency key đã được dùng cho yêu cầu khác");
            }
            return toResponse(existing);
        }
        for (var line : request.rooms()) {
            if (locked.get(line.roomId()).getStatus().blocksAvailability()) throw error("ROOM_NOT_AVAILABLE", "Phòng không sẵn sàng: " + line.roomId());
            if (reservations.hasOverlap(line.roomId(), line.expectedCheckIn(), line.expectedCheckOut(), RoomStatus.CANCELLED, IGNORED)) throw error("OVERBOOKING", "Phòng đã có lịch trùng: " + line.roomId());
        }
        BigDecimal requiredDeposit = requiredDeposit(request, locked);
        BigDecimal suppliedDeposit = request.deposit() == null ? BigDecimal.ZERO : request.deposit();
        if (requiredDeposit.signum() > 0 && suppliedDeposit.compareTo(requiredDeposit) != 0)
            throw error("INVALID_DEPOSIT", "Tiền đặt cọc phải bằng 50% tiền phòng dự kiến");
        Reservation reservation = new Reservation(); reservation.setGuest(guest); reservation.setEmployee(employee);
        reservation.setDepositAmount(suppliedDeposit); reservation.setRentalType(request.rentalType().name()); reservation.setIdempotencyKey(key);
        reservation.setCanonicalRequestFingerprint(fingerprint);
        reservation.transitionTo(reservation.getDepositAmount().signum() > 0 ? ReservationStatus.DEPOSIT_PAID : ReservationStatus.CONFIRMED);
        request.rooms().forEach(line -> { ReservationRoom rr = new ReservationRoom(); rr.setRoom(locked.get(line.roomId())); rr.setCheckIn(line.expectedCheckIn()); rr.setCheckOut(line.expectedCheckOut()); rr.setStatus(RoomStatus.RESERVED); reservation.addRoom(rr); });
        try {
            Reservation saved = reservations.saveAndFlush(reservation);
            billing.registerDeposit(saved);
            audit.record(actor, "RESERVATION_CREATED", "RESERVATION", saved.getId().toString(), null,
                    saved.getStatus().name(), fingerprint);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            var duplicate = reservations.findByIdempotencyKey(key);
            if (duplicate.isPresent() && actor.equals(duplicate.get().getEmployee().getEmployeeId())
                    && fingerprint.equals(createFingerprint(duplicate.get()))) return toResponse(duplicate.get());
            throw ex;
        }
    }

    @Transactional(readOnly=true) public ReservationDtos.Response get(Long id) { return reservations.findDetails(id).map(this::toResponse).orElseThrow(() -> error("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng: " + id)); }
    @Transactional(readOnly = true)
    public ReservationDtos.PageResponse list(ReservationStatus status, Long guestId, int page, int size) {
        String actor = SecurityActor.currentActor();
        boolean global = ReservationAccess.hasGlobalRead();
        if (page < 0 || size < 1 || size > 100 || (guestId != null && guestId <= 0))
            throw error("INVALID_SEARCH", "page phải >= 0, size từ 1 đến 100 và guest_id phải > 0");
        var ids = reservations.searchIds(global ? null : actor, status, guestId,
                org.springframework.data.domain.PageRequest.of(page, size));
        Map<Long, Reservation> details = new HashMap<>();
        if (!ids.isEmpty()) reservations.findPageDetails(ids.getContent()).forEach(r -> details.put(r.getId(), r));
        var items = ids.getContent().stream().map(details::get).map(this::toResponse).toList();
        return new ReservationDtos.PageResponse(items, page, size, ids.getTotalElements(), ids.getTotalPages());
    }

    @Transactional
    public ReservationDtos.Response checkIn(Long id, ReservationDtos.CheckInRequest request, String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String fingerprint = IdempotencySupport.fingerprint("CHECK_IN|" + id + "|" + (request == null ? "" : request.at()));
        return idempotency.execute("reservation-check-in", key, actor, fingerprint, () -> {
            Reservation r = locked(id);
            requireState(r, ReservationStatus.CONFIRMED, ReservationStatus.DEPOSIT_PAID);
            LocalDateTime at = request == null || request.at() == null ? LocalDateTime.now(clock) : request.at();
            Map<String, Room> lockedRooms = lockRooms(r.getRooms().stream().map(x -> x.getRoom().getId()).toList());
            for (ReservationRoom rr : r.getRooms()) {
                if (at.isBefore(rr.getCheckIn())) throw error("EARLY_CHECK_IN", "Không thể nhận phòng trước giờ dự kiến");
                if (!at.isBefore(rr.getCheckOut())) throw error("INVALID_CHECK_IN_TIME", "Giờ nhận phải trước giờ trả dự kiến");
                Room room = lockedRooms.get(rr.getRoom().getId());
                if (room.getStatus() != RoomStatus.READY && room.getStatus() != RoomStatus.RESERVED)
                    throw error("ROOM_NOT_AVAILABLE", "Phòng chưa sẵn sàng để nhận khách");
                if (reservations.hasOverlapExcludingReservation(id, room.getId(), at, rr.getCheckOut(),
                        RoomStatus.CANCELLED, IGNORED)) throw error("OVERBOOKING", "Phòng có lịch đặt giao nhau");
            }
            for (ReservationRoom rr : r.getRooms()) {
                rr.setStatus(RoomStatus.OCCUPIED);
                rr.getRoom().setStatus(RoomStatus.OCCUPIED);
            }
            r.setActualCheckIn(at);
            r.transitionTo(ReservationStatus.CHECKED_IN);
            audit.record(actor, "RESERVATION_CHECKED_IN", "RESERVATION", id.toString(), null, at.toString(), null);
            return toResponse(r);
        });
    }

    @Transactional
    public InvoiceDtos.Response checkOut(Long id, ReservationDtos.CheckOutRequest request, String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String fingerprint = IdempotencySupport.fingerprint("CHECK_OUT|" + id + "|" + (request == null ? "" : request.at())
                + "|" + (request == null ? "" : request.paymentMethod()));
        return idempotency.execute("reservation-check-out", key, actor, fingerprint, () -> {
            Reservation r = locked(id);
            requireState(r, ReservationStatus.CHECKED_IN);
            List<LocalDateTime> scheduled = r.getRooms().stream().map(ReservationRoom::getCheckOut).toList();
            InvoiceDtos.Response response = billing.checkOut(id, request == null ? null : request.at(),
                    request == null ? null : request.paymentMethod(), actor);
            // Billing calculates against the scheduled checkout, but must not
            // replace that schedule with the actual checkout timestamp.
            for (int i = 0; i < r.getRooms().size(); i++) r.getRooms().get(i).setCheckOut(scheduled.get(i));
            return response;
        });
    }

    @Transactional
    public ReservationDtos.Response cancel(Long id, ReservationDtos.CancelRequest request,
                                           String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String reason = request == null ? null : request.reason();
        String fingerprint = IdempotencySupport.fingerprint("CANCEL|" + id + "|" + (reason == null ? "" : reason.trim()));
        return idempotency.execute("reservation-cancel", key, actor, fingerprint, () -> {
            if (reason == null || reason.isBlank()) throw error("CANCELLATION_REASON_REQUIRED", "Hủy đặt phòng phải có lý do");
            Reservation r = locked(id);
            requireState(r, ReservationStatus.DRAFT, ReservationStatus.CONFIRMED, ReservationStatus.DEPOSIT_PAID);
            LocalDateTime now = LocalDateTime.now(clock);
            LocalDateTime checkIn = r.getRooms().stream().map(ReservationRoom::getCheckIn).min(LocalDateTime::compareTo)
                    .orElseThrow(() -> error("INVALID_RESERVATION", "Đặt phòng không có phòng"));
            LocalDateTime scheduledCheckout = r.getRooms().stream().map(ReservationRoom::getCheckOut).max(LocalDateTime::compareTo)
                    .orElseThrow(() -> error("INVALID_RESERVATION", "Đặt phòng không có phòng"));
            if (r.getActualCheckIn() == null && !now.isBefore(scheduledCheckout)) {
                r.transitionTo(ReservationStatus.NO_SHOW);
                r.getRooms().forEach(x -> x.setStatus(RoomStatus.CANCELLED));
                r.setCancellationReason(reason.trim());
                r.setCancellationOutcome(CancellationOutcome.FORFEIT);
                audit.record(actor, "RESERVATION_NO_SHOW", "RESERVATION", id.toString(), null,
                        ReservationStatus.NO_SHOW.name(), "DEPOSIT_FORFEITED");
                billing.settleCancellationDeposit(r, actor, true);
                return toResponse(r);
            }
            boolean late = !now.isBefore(checkIn.minusHours(lateCancellationHours)) && now.isBefore(checkIn);
            CancellationOutcome outcome = late ? CancellationOutcome.FORFEIT
                    : (r.getDepositAmount() != null && r.getDepositAmount().signum() > 0
                    ? CancellationOutcome.REFUND : CancellationOutcome.RETAIN);
            r.transitionTo(ReservationStatus.CANCELLED);
            r.getRooms().forEach(x -> x.setStatus(RoomStatus.CANCELLED));
            if (late) bookingPolicy.recordLateCancellation(r.getGuest());
            r.setCancellationReason(reason.trim());
            r.setCancellationOutcome(outcome);
            // Billing remains the owner of invoices/payment transactions.
            billing.settleCancellationDeposit(r, actor, outcome == CancellationOutcome.FORFEIT);
            audit.record(actor, "RESERVATION_CANCELLED", "RESERVATION", id.toString(), null,
                    outcome.name(), r.getCancellationReason());
            return toResponse(r);
        });
    }

    @Transactional
    public ReservationDtos.Response extend(Long id, ReservationDtos.ExtendRequest request,
                                           String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String fingerprint = IdempotencySupport.fingerprint("EXTEND|" + id + "|"
                + (request == null ? "" : request.newExpectedCheckOut()));
        return idempotency.execute("reservation-extend", key, actor, fingerprint, () -> {
            Reservation r = locked(id);
            requireState(r, ReservationStatus.CONFIRMED, ReservationStatus.DEPOSIT_PAID, ReservationStatus.CHECKED_IN);
            if (request == null || request.newExpectedCheckOut() == null)
                throw error("INVALID_INTERVAL", "Thời gian gia hạn không hợp lệ");
            LocalDateTime newCheckout = request.newExpectedCheckOut();
            lockRooms(r.getRooms().stream().map(x -> x.getRoom().getId()).toList());
            int delta = -1;
            for (ReservationRoom rr : r.getRooms()) {
                if (!newCheckout.isAfter(rr.getCheckOut())) throw error("INVALID_EXTENSION", "Giờ trả mới phải sau giờ trả hiện tại");
                if (LocalDateTime.now(clock).isAfter(rr.getCheckOut().minusHours(1)))
                    throw error("EXTENSION_TOO_LATE", "Chỉ được gia hạn trước giờ trả ít nhất 1 tiếng");
                if (reservations.hasOverlapExcludingReservation(id, rr.getRoom().getId(), rr.getCheckIn(), newCheckout,
                        RoomStatus.CANCELLED, IGNORED)) throw error("OVERBOOKING", "Khung giờ gia hạn đã có lịch đặt");
                if (delta < 0) delta = (int) Duration.between(rr.getCheckOut(), newCheckout).toMinutes();
            }
            r.setExtensionMinutes(Math.addExact(r.getExtensionMinutes(), delta));
            r.getRooms().forEach(rr -> rr.setCheckOut(newCheckout));
            audit.record(actor, "RESERVATION_EXTENDED", "RESERVATION", id.toString(), null, newCheckout.toString(), null);
            return toResponse(r);
        });
    }

    /**
     * Closes an unarrived reservation after its scheduled stay has ended.
     * The deposit remains collected; there is deliberately no refund path here.
     */
    @Transactional
    public ReservationDtos.Response markNoShow(Long id, String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String fingerprint = IdempotencySupport.fingerprint("NO_SHOW|" + id);
        return idempotency.execute("reservation-no-show", key, actor, fingerprint, () -> {
            Reservation r = locked(id);
            requireState(r, ReservationStatus.CONFIRMED, ReservationStatus.DEPOSIT_PAID);
            if (r.getActualCheckIn() != null)
                throw error("INVALID_STATE", "Đặt phòng đã nhận phòng, không thể đánh dấu no-show");
            LocalDateTime scheduledCheckout = r.getRooms().stream().map(ReservationRoom::getCheckOut)
                    .max(LocalDateTime::compareTo)
                    .orElseThrow(() -> error("INVALID_RESERVATION", "Đặt phòng không có phòng"));
            if (LocalDateTime.now(clock).isBefore(scheduledCheckout))
                throw error("NO_SHOW_TOO_EARLY", "Chỉ được đánh dấu no-show sau khi hết thời gian đặt phòng");
            r.transitionTo(ReservationStatus.NO_SHOW);
            r.getRooms().forEach(x -> x.setStatus(RoomStatus.CANCELLED));
            audit.record(actor, "RESERVATION_NO_SHOW", "RESERVATION", id.toString(), null,
                    ReservationStatus.NO_SHOW.name(), "DEPOSIT_FORFEITED");
            return toResponse(r);
        });
    }

    @Transactional
    public ReservationDtos.Response addService(Long id, ReservationDtos.AddServiceRequest request,
                                               String suppliedActor, String key) {
        String actor = authenticatedActor(suppliedActor);
        ReservationAccess.requireOperator(actor);
        String fingerprint = IdempotencySupport.fingerprint("SERVICE|" + id + "|" + request.serviceId() + "|"
                + request.quantity() + "|" + request.usedAt());
        return idempotency.execute("reservation-service", key, actor, fingerprint, () -> {
            Reservation r = locked(id);
            requireState(r, ReservationStatus.CHECKED_IN);
            Service service = serviceCatalog.findWithLockById(request.serviceId()).orElseThrow(() -> error("SERVICE_NOT_FOUND", "Không tìm thấy dịch vụ"));
            if (service.getStockQuantity() < request.quantity()) throw error("INSUFFICIENT_STOCK", "Tồn kho không đủ");
            var usedOn = (request.usedAt() == null ? LocalDateTime.now(clock) : request.usedAt()).toLocalDate();
            var lineKey = new ServiceUsageId(id, request.serviceId(), usedOn);
            var line = serviceLines.findById(lineKey).orElseGet(ServiceUsage::new);
            line.setReservation(r); line.setService(service); line.setUsedOn(usedOn);
            if (line.getUnitPrice() == null) line.setUnitPrice(service.getPrice());
            line.setQuantity(line.getQuantity() + request.quantity());
            serviceLines.save(line);
            service.setStockQuantity(service.getStockQuantity() - request.quantity());
            if (inventoryMovements != null) {
                InventoryMovement movement = new InventoryMovement(); movement.setService(service);
                movement.setType(InventoryMovement.MovementType.ISSUE); movement.setQuantity(request.quantity());
                movement.setActorId(actor); movement.setOccurredAt(LocalDateTime.now(clock));
                movement.setReason("RESERVATION_SERVICE:" + id); inventoryMovements.save(movement);
            }
            audit.record(actor, "SERVICE_ADDED", "RESERVATION", id.toString(), null, request.serviceId(), null);
            return toResponse(r);
        });
    }
    private Guest sharedGuest(Long id) { return sharedGuests.findSharedById(id).orElseThrow(() -> error("GUEST_NOT_FOUND", "Không tìm thấy khách hàng")); }
    private Reservation locked(Long id){return reservations.findForUpdate(id).orElseThrow(()->error("RESERVATION_NOT_FOUND","Không tìm thấy đặt phòng"));}
    private Map<String,Room> lockRooms(Collection<String> ids){Map<String,Room> out=new HashMap<>(); var sorted=ids.stream().distinct().sorted().toList(); for(Room room:rooms.findAllForUpdateOrdered(sorted))out.put(room.getId(),room); for(String id:sorted)if(!out.containsKey(id))throw error("ROOM_NOT_FOUND","Không tìm thấy phòng: "+id); return out;}
    private String effectiveKey(String bodyKey, String headerKey) {
        if (headerKey != null && !headerKey.isBlank() && bodyKey != null && !bodyKey.isBlank()
                && !headerKey.trim().equals(bodyKey.trim()))
            throw error("IDEMPOTENCY_KEY_CONFLICT", "Idempotency key trong header và nội dung không giống nhau");
        return IdempotencySupport.requireKey(headerKey == null || headerKey.isBlank() ? bodyKey : headerKey);
    }

    private String createFingerprint(ReservationDtos.CreateRequest request) {
        String rooms = request.rooms().stream().map(line -> line.roomId().trim() + "@"
                        + line.expectedCheckIn() + "/" + line.expectedCheckOut())
                .sorted().reduce((a, b) -> a + ";" + b).orElse("");
        return IdempotencySupport.fingerprint("CREATE|guest=" + request.guestId() + "|employee="
                + request.employeeId().trim() + "|deposit=" + (request.deposit() == null ? BigDecimal.ZERO : request.deposit().stripTrailingZeros().toPlainString())
                + "|rental=" + request.rentalType().name() + "|rooms=" + rooms);
    }

    private String createFingerprint(Reservation reservation) {
        if (reservation.getCanonicalRequestFingerprint() != null && !reservation.getCanonicalRequestFingerprint().isBlank())
            return reservation.getCanonicalRequestFingerprint();
        String rooms = reservation.getRooms().stream().map(line -> line.getRoom().getId() + "@"
                        + line.getCheckIn() + "/" + line.getCheckOut())
                .sorted().reduce((a, b) -> a + ";" + b).orElse("");
        return IdempotencySupport.fingerprint("CREATE|guest=" + reservation.getGuest().getId() + "|employee="
                + reservation.getEmployee().getEmployeeId() + "|deposit="
                + (reservation.getDepositAmount() == null ? BigDecimal.ZERO : reservation.getDepositAmount().stripTrailingZeros().toPlainString())
                + "|rental=" + reservation.getRentalType() + "|rooms=" + rooms);
    }
    private BigDecimal requiredDeposit(ReservationDtos.CreateRequest request, Map<String, Room> locked) {
        BigDecimal total = BigDecimal.ZERO;
        for (var line : request.rooms()) {
            var roomType = locked.get(line.roomId()).getRoomType();
            if (roomType == null || roomType.getDailyPrice() == null) return BigDecimal.ZERO;
            long minutes = Math.max(1, Duration.between(line.expectedCheckIn(), line.expectedCheckOut()).toMinutes());
            long units = "HOURLY".equals(request.rentalType().name())
                    ? Math.max(3, (minutes + 59) / 60)
                    : Math.max(1, (minutes + 1439) / 1440);
            BigDecimal charge = "HOURLY".equals(request.rentalType().name())
                    ? roomType.getDailyPrice().divide(BigDecimal.valueOf(24), 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(units))
                    : roomType.getDailyPrice().multiply(BigDecimal.valueOf(units));
            total = total.add(charge);
        }
        return total.multiply(new BigDecimal("0.50")).setScale(2, java.math.RoundingMode.HALF_UP);
    }
    private String authenticatedActor(String supplied) {
        return SecurityActor.requireBoundActor(supplied);
    }
    private void requireActor(String actor,String owner){if(actor==null||actor.isBlank()||owner==null||!owner.equals(actor))throw error("ACTOR_MISMATCH","Actor không khớp nhân viên của đặt phòng");}
    private void requireState(Reservation r,ReservationStatus... allowed){if(Arrays.stream(allowed).noneMatch(s->s==r.getStatus()))throw error("INVALID_STATE","Đặt phòng không thể thực hiện ở trạng thái hiện tại");}
    private DomainException error(String code,String message){return new DomainException(code,message);}
    public ReservationDtos.Response toResponse(Reservation r){return new ReservationDtos.Response(r.getId(),r.getGuest().getId(),r.getEmployee().getEmployeeId(),r.getStatus(),ReservationDtos.RentalType.valueOf(r.getRentalType()),r.getDepositAmount(),r.getBookedAt(),r.getActualCheckIn(),r.getActualCheckOut(),r.getRooms().stream().map(x->new ReservationDtos.RoomLine(x.getRoom().getId(),x.getCheckIn(),x.getCheckOut(),r.getActualCheckIn(),r.getActualCheckOut())).toList(),r.getCancellationReason(),r.getCancellationOutcome());}
}
