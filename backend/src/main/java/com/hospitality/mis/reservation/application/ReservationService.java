package com.hospitality.mis.reservation.application;

import com.hospitality.mis.billing.application.BillingService;
import com.hospitality.mis.billing.adapter.ServiceLineRepository;
import com.hospitality.mis.billing.adapter.ServiceRepository;
import com.hospitality.mis.billing.domain.ChiTietDichVu;
import com.hospitality.mis.billing.domain.ChiTietDichVuId;
import com.hospitality.mis.billing.domain.Service;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.guest.adapter.GuestRepository;
import com.hospitality.mis.identity.adapter.EmployeeRepository;
import com.hospitality.mis.guest.domain.Guest;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.reservation.adapter.ReservationRepository;
import com.hospitality.mis.reservation.api.ReservationDtos;
import com.hospitality.mis.reservation.domain.*;
import com.hospitality.mis.room.adapter.RoomRepository;
import com.hospitality.mis.room.domain.Room;
import com.hospitality.mis.room.domain.RoomStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
public class ReservationService {
    private static final List<ReservationStatus> IGNORED = List.of(ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW, ReservationStatus.CHECKED_OUT);
    private final ReservationRepository reservations; private final GuestRepository guests; private final EmployeeRepository employees;
    private final RoomRepository rooms; private final AuditService audit; private final BillingService billing;
    private final ServiceRepository serviceCatalog; private final ServiceLineRepository serviceLines;
    public ReservationService(ReservationRepository reservations, GuestRepository guests, EmployeeRepository employees, RoomRepository rooms,
                              AuditService audit, BillingService billing, ServiceRepository serviceCatalog, ServiceLineRepository serviceLines) {
        this.reservations=reservations; this.guests=guests; this.employees=employees; this.rooms=rooms; this.audit=audit; this.billing=billing; this.serviceCatalog=serviceCatalog; this.serviceLines=serviceLines;
    }

    @Transactional
    public synchronized ReservationDtos.Response create(ReservationDtos.CreateRequest request, String actor) {
        requireActor(actor, request.employeeId());
        if (request.idempotencyKey() != null) {
            var prior = reservations.findByIdempotencyKeyForUpdate(request.idempotencyKey());
            if (prior.isPresent()) return toResponse(prior.get());
        }
        if (request.rooms().size() > 3) throw error("ROOM_LIMIT_EXCEEDED", "Mỗi lượt lưu trú chỉ được đặt tối đa 3 phòng");
        Guest guest = guests.findById(request.guestId()).orElseThrow(() -> error("GUEST_NOT_FOUND", "Không tìm thấy khách hàng"));
        if (!guest.canPlaceBooking()) throw error("GUEST_BOOKING_BLOCKED", "Khách hàng đã bị khóa quyền đặt trước");
        Employee employee = employees.findById(request.employeeId()).orElseThrow(() -> error("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên"));
        Set<String> ids = new HashSet<>();
        for (var line : request.rooms()) {
            if (!line.expectedCheckIn().isBefore(line.expectedCheckOut())) throw error("INVALID_INTERVAL", "Thời gian nhận phải trước thời gian trả");
            if (!ids.add(line.roomId())) throw error("DUPLICATE_ROOM", "Không được lặp phòng trong một đơn đặt");
            if (request.rentalType() == ReservationDtos.RentalType.HOURLY && Duration.between(line.expectedCheckIn(), line.expectedCheckOut()).toMinutes() < 180) throw error("HOURLY_MINIMUM", "Thuê theo giờ tối thiểu 3 tiếng");
        }
        Map<String, Room> locked = lockRooms(ids);
        for (var line : request.rooms()) {
            if (locked.get(line.roomId()).getStatus().blocksAvailability()) throw error("ROOM_NOT_AVAILABLE", "Phòng không sẵn sàng: " + line.roomId());
            if (reservations.hasOverlap(line.roomId(), line.expectedCheckIn(), line.expectedCheckOut(), RoomStatus.CANCELLED, IGNORED)) throw error("OVERBOOKING", "Phòng đã có lịch trùng: " + line.roomId());
        }
        Reservation reservation = new Reservation(); reservation.setGuest(guest); reservation.setEmployee(employee);
        reservation.setDepositAmount(request.deposit() == null ? BigDecimal.ZERO : request.deposit()); reservation.setRentalType(request.rentalType().name()); reservation.setIdempotencyKey(request.idempotencyKey());
        reservation.transitionTo(reservation.getDepositAmount().signum() > 0 ? ReservationStatus.DEPOSIT_PAID : ReservationStatus.CONFIRMED);
        request.rooms().forEach(line -> { ReservationRoom rr = new ReservationRoom(); rr.setRoom(locked.get(line.roomId())); rr.setCheckIn(line.expectedCheckIn()); rr.setCheckOut(line.expectedCheckOut()); rr.setStatus(RoomStatus.RESERVED); reservation.addRoom(rr); });
        try {
            Reservation saved = reservations.saveAndFlush(reservation);
            audit.record(actor, "RESERVATION_CREATED", "RESERVATION", saved.getId().toString(), null, saved.getStatus().name(), null);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            return reservations.findByIdempotencyKey(request.idempotencyKey()).map(this::toResponse).orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly=true) public ReservationDtos.Response get(Long id) { return reservations.findDetails(id).map(this::toResponse).orElseThrow(() -> error("RESERVATION_NOT_FOUND", "Không tìm thấy đặt phòng: " + id)); }

    @Transactional public ReservationDtos.Response checkIn(Long id, ReservationDtos.CheckInRequest request, String actor) {
        Reservation r = locked(id); requireActor(actor, r.getEmployee().getEmployeeId()); requireState(r, ReservationStatus.CONFIRMED, ReservationStatus.DEPOSIT_PAID);
        LocalDateTime at = request == null || request.at()==null ? LocalDateTime.now() : request.at();
        for (ReservationRoom rr:r.getRooms()) { if (at.isBefore(rr.getCheckIn())) throw error("EARLY_CHECK_IN", "Không thể nhận phòng trước giờ dự kiến"); rr.setStatus(RoomStatus.OCCUPIED); rr.getRoom().setStatus(RoomStatus.OCCUPIED); }
        r.setActualCheckIn(at); r.transitionTo(ReservationStatus.CHECKED_IN); audit.record(actor,"RESERVATION_CHECKED_IN","RESERVATION",id.toString(),null,at.toString(),null); return toResponse(r);
    }
    @Transactional public com.hospitality.mis.billing.api.InvoiceDtos.Response checkOut(Long id, ReservationDtos.CheckOutRequest request, String actor) { Reservation r=locked(id); requireActor(actor,r.getEmployee().getEmployeeId()); requireState(r,ReservationStatus.CHECKED_IN); return billing.checkOut(id,request==null?null:request.at(),request==null?null:request.paymentMethod(),actor); }
    @Transactional public ReservationDtos.Response cancel(Long id,String actor) { Reservation r=locked(id); requireActor(actor,r.getEmployee().getEmployeeId()); if(r.getStatus()==ReservationStatus.CANCELLED)return toResponse(r); requireState(r,ReservationStatus.DRAFT,ReservationStatus.CONFIRMED,ReservationStatus.DEPOSIT_PAID); r.transitionTo(ReservationStatus.CANCELLED); r.getRooms().forEach(x->x.setStatus(RoomStatus.CANCELLED)); if(r.getRooms().stream().anyMatch(x->Duration.between(LocalDateTime.now(),x.getCheckIn()).toHours()<24)) r.getGuest().recordLateCancellation(4); audit.record(actor,"RESERVATION_CANCELLED","RESERVATION",id.toString(),null,"CANCELLED",null); return toResponse(r); }
    @Transactional public ReservationDtos.Response extend(Long id,ReservationDtos.ExtendRequest request,String actor) { Reservation r=locked(id); requireActor(actor,r.getEmployee().getEmployeeId()); requireState(r,ReservationStatus.CONFIRMED,ReservationStatus.DEPOSIT_PAID); if(request==null||request.newExpectedCheckOut()==null)throw error("INVALID_INTERVAL","Thời gian gia hạn không hợp lệ"); lockRooms(r.getRooms().stream().map(x->x.getRoom().getId()).toList()); for(ReservationRoom rr:r.getRooms()){ if(!request.newExpectedCheckOut().isAfter(rr.getCheckOut()))throw error("INVALID_EXTENSION","Giờ trả mới phải sau giờ trả hiện tại"); if(LocalDateTime.now().isAfter(rr.getCheckOut().minusHours(1)))throw error("EXTENSION_TOO_LATE","Chỉ được gia hạn trước giờ trả ít nhất 1 tiếng"); if(reservations.hasOverlapExcludingReservation(id,rr.getRoom().getId(),rr.getCheckIn(),request.newExpectedCheckOut(),RoomStatus.CANCELLED,IGNORED))throw error("OVERBOOKING","Khung giờ gia hạn đã có lịch đặt"); r.setExtensionMinutes(r.getExtensionMinutes()+(int)Duration.between(rr.getCheckOut(),request.newExpectedCheckOut()).toMinutes()); rr.setCheckOut(request.newExpectedCheckOut()); } audit.record(actor,"RESERVATION_EXTENDED","RESERVATION",id.toString(),null,request.newExpectedCheckOut().toString(),null); return toResponse(r); }
    @Transactional public ReservationDtos.Response addService(Long id,ReservationDtos.AddServiceRequest request,String actor){ Reservation r=locked(id); requireActor(actor,r.getEmployee().getEmployeeId()); requireState(r,ReservationStatus.CHECKED_IN); Service service=serviceCatalog.findWithLockById(request.serviceId()).orElseThrow(()->error("SERVICE_NOT_FOUND","Không tìm thấy dịch vụ")); if(service.getStockQuantity()<request.quantity())throw error("INSUFFICIENT_STOCK","Tồn kho không đủ"); var usedOn=(request.usedAt()==null?LocalDateTime.now():request.usedAt()).toLocalDate(); var key=new ChiTietDichVuId(id,request.serviceId(),usedOn); var line=serviceLines.findById(key).orElseGet(ChiTietDichVu::new); line.setReservation(r); line.setService(service); line.setUsedOn(usedOn); line.setQuantity(line.getQuantity()+request.quantity()); serviceLines.save(line); service.setStockQuantity(service.getStockQuantity()-request.quantity()); audit.record(actor,"SERVICE_ADDED","RESERVATION",id.toString(),null,request.serviceId(),null); return toResponse(r); }
    private Reservation locked(Long id){return reservations.findForUpdate(id).orElseThrow(()->error("RESERVATION_NOT_FOUND","Không tìm thấy đặt phòng"));}
    private Map<String,Room> lockRooms(Collection<String> ids){Map<String,Room> out=new HashMap<>(); var sorted=ids.stream().distinct().sorted().toList(); for(Room room:rooms.findAllForUpdateOrdered(sorted))out.put(room.getId(),room); for(String id:sorted)if(!out.containsKey(id))throw error("ROOM_NOT_FOUND","Không tìm thấy phòng: "+id); return out;}
    private void requireActor(String actor,String owner){if(actor==null||actor.isBlank()||owner==null||!owner.equals(actor))throw error("ACTOR_MISMATCH","Actor không khớp nhân viên của đặt phòng");}
    private void requireState(Reservation r,ReservationStatus... allowed){if(Arrays.stream(allowed).noneMatch(s->s==r.getStatus()))throw error("INVALID_STATE","Đặt phòng không thể thực hiện ở trạng thái hiện tại");}
    private DomainException error(String code,String message){return new DomainException(code,message);}
    public ReservationDtos.Response toResponse(Reservation r){return new ReservationDtos.Response(r.getId(),r.getGuest().getId(),r.getEmployee().getEmployeeId(),r.getStatus(),ReservationDtos.RentalType.valueOf(r.getRentalType()),r.getDepositAmount(),r.getBookedAt(),r.getActualCheckIn(),r.getActualCheckOut(),r.getRooms().stream().map(x->new ReservationDtos.RoomLine(x.getRoom().getId(),x.getCheckIn(),x.getCheckOut(),r.getActualCheckIn(),r.getActualCheckOut())).toList());}
}
