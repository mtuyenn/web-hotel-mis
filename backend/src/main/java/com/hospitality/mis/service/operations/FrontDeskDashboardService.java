package com.hospitality.mis.service.operations;

import com.hospitality.mis.dao.operations.EquipmentIncidentRepository;
import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.dao.room.RoomRepository;
import com.hospitality.mis.dto.operations.FrontDeskDashboardDtos;
import com.hospitality.mis.entity.billing.Invoice;
import com.hospitality.mis.entity.reservation.Reservation;
import com.hospitality.mis.entity.reservation.ReservationRoom;
import com.hospitality.mis.entity.reservation.ReservationStatus;
import com.hospitality.mis.entity.room.Room;
import com.hospitality.mis.entity.room.RoomStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/** Tổng hợp một lần đọc các chỉ báo vận hành mà lễ tân cần trong ngày. */
@Service
public class FrontDeskDashboardService {
    private final ReservationRepository reservations;
    private final RoomRepository rooms;
    private final EquipmentIncidentRepository incidents;
    private final Clock clock;

    public FrontDeskDashboardService(ReservationRepository reservations, RoomRepository rooms,
                                     EquipmentIncidentRepository incidents, Clock clock) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.incidents = incidents;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public FrontDeskDashboardDtos.Response get(LocalDate date, String query, ReservationStatus status,
                                               int page, int size) {
        LocalDate businessDate = date == null ? LocalDate.now(clock) : date;
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        List<Reservation> all = reservations.findAll();
        List<FrontDeskDashboardDtos.ReservationItem> items = all.stream()
                .map(this::toItem)
                .filter(x -> status == null || x.status() == status)
                .filter(x -> normalized.isBlank() || String.valueOf(x.reservationId()).contains(normalized)
                        || (x.guestName() != null && x.guestName().toLowerCase(Locale.ROOT).contains(normalized))
                        || (x.guestPhone() != null && x.guestPhone().contains(normalized))
                        || x.roomIds().stream().anyMatch(r -> r.toLowerCase(Locale.ROOT).contains(normalized)))
                .sorted(Comparator.comparing(FrontDeskDashboardDtos.ReservationItem::checkIn,
                        Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(FrontDeskDashboardDtos.ReservationItem::reservationId))
                .toList();

        List<FrontDeskDashboardDtos.ReservationItem> arrivals = items.stream()
                .filter(x -> x.checkIn() != null && x.checkIn().toLocalDate().equals(businessDate)
                        && (x.status() == ReservationStatus.CONFIRMED || x.status() == ReservationStatus.DEPOSIT_PAID))
                .toList();
        List<FrontDeskDashboardDtos.ReservationItem> departures = items.stream()
                .filter(x -> x.checkOut() != null && x.checkOut().toLocalDate().equals(businessDate)
                        && x.status() == ReservationStatus.CHECKED_IN).toList();
        List<FrontDeskDashboardDtos.ReservationItem> current = items.stream()
                .filter(x -> x.status() == ReservationStatus.CHECKED_IN).toList();
        List<FrontDeskDashboardDtos.ReservationItem> unpaidDeposits = items.stream()
                .filter(x -> "PENDING".equals(x.depositPaymentStatus())
                        || (x.depositAmount() != null && x.depositAmount().signum() > 0
                        && !"PAID".equals(x.depositPaymentStatus()))).toList();
        List<FrontDeskDashboardDtos.ReservationItem> balances = items.stream()
                .filter(x -> x.invoiceBalance() != null && x.invoiceBalance().signum() > 0).toList();

        List<FrontDeskDashboardDtos.ReservationItem> paged = items.stream()
                .skip((long) safePage * safeSize).limit(safeSize).toList();
        List<FrontDeskDashboardDtos.RoomSummary> roomItems = rooms.search(null, null).stream()
                .map(r -> new FrontDeskDashboardDtos.RoomSummary(r.getId(), r.getName(), r.getStatus(), r.getRoomType().getId()))
                .toList();
        Map<String, Long> roomCounts = roomItems.stream().collect(Collectors.groupingBy(
                x -> x.status().databaseCode(), TreeMap::new, Collectors.counting()));
        List<FrontDeskDashboardDtos.IncidentItem> incidentItems = incidents.findAll().stream()
                .map(i -> new FrontDeskDashboardDtos.IncidentItem(i.getId(),
                        i.getReservation() == null ? null : i.getReservation().getId(),
                        i.getRoom() == null ? null : i.getRoom().getId(), i.getCompensation()))
                .toList();

        int totalPages = (int) Math.ceil(items.size() / (double) safeSize);
        return new FrontDeskDashboardDtos.Response(businessDate, arrivals, departures, current,
                unpaidDeposits, balances, roomItems, roomCounts, incidentItems,
                safePage, safeSize, items.size(), totalPages);
    }

    private FrontDeskDashboardDtos.ReservationItem toItem(Reservation r) {
        LocalDateTime checkIn = r.getRooms().stream().map(ReservationRoom::getCheckIn)
                .min(LocalDateTime::compareTo).orElse(null);
        LocalDateTime checkOut = r.getRooms().stream().map(ReservationRoom::getCheckOut)
                .max(LocalDateTime::compareTo).orElse(null);
        Invoice invoice = r.getInvoice();
        return new FrontDeskDashboardDtos.ReservationItem(r.getId(),
                r.getGuest() == null ? null : r.getGuest().getId(),
                r.getGuest() == null ? null : r.getGuest().getFullName(),
                r.getGuest() == null ? null : r.getGuest().getPhone(), r.getStatus(), checkIn, checkOut,
                r.getRooms().stream().map(x -> x.getRoom().getId()).toList(), r.getDepositAmount(),
                r.getDepositPaymentStatus() == null ? null : r.getDepositPaymentStatus().name(),
                invoice == null ? BigDecimal.ZERO : invoice.getAmountDue());
    }
}
