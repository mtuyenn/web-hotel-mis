package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.hotelmanagement.service.HoaDonService;
import com.hotelmanagement.service.PhongService;
import com.hotelmanagement.service.impl.HoaDonServiceImpl;
import com.hotelmanagement.service.impl.PhongServiceImpl;
import com.hotelmanagement.model.dto.PhongDTO;
import com.hotelmanagement.model.dto.HoaDonDTO;

/**
 * Lớp OverviewPanel - Panel hiển thị tổng quan về tình hình hoạt động của khách
 * sạn
 * 
 * Chức năng:
 * - Hiển thị các chỉ số KPI chính: tổng số phòng, tỉ lệ đặt phòng, giá trung
 * bình, doanh thu
 * - Hiển thị biểu đồ công suất phòng theo thời gian
 * - Hiển thị biểu đồ trạng thái phòng hiện tại
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * - Hỗ trợ lọc dữ liệu theo thời gian (ngày, tháng, năm)
 * 
 * Thiết kế:
 * - Giao diện chia thành 2 phần: KPI cards (trên) và Charts (dưới)
 * - Sử dụng BorderLayout để sắp xếp các thành phần
 * - Sử dụng GridLayout cho KPI cards
 * - Sử dụng CardLayout cho các biểu đồ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class OverviewPanel extends JPanel {

    private SimpleChartPanel occupancyChart;
    private SimpleChartPanel roomStatusChart;

    private KpiCardPanel totalRoomsCard;
    private KpiCardPanel occupancyRateCard;
    private KpiCardPanel adrCard;
    private KpiCardPanel revenueCard;

    public OverviewPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252)); // Slate 50

        // Main content container with ScrollPane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 25));
        contentPanel.setBackground(new Color(248, 250, 252));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 1. KPI Cards Top Section
        contentPanel.add(createKpiSection(), BorderLayout.NORTH);

        // 2. Charts Section
        contentPanel.add(createChartsSection(), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Load data from DB
        loadRealData();
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiPanel.setBackground(new Color(248, 250, 252));

        totalRoomsCard = new KpiCardPanel("TỔNG SỐ PHÒNG", "0", "", true);
        occupancyRateCard = new KpiCardPanel("TỈ LỆ ĐẶT PHÒNG", "0%", "", true);
        adrCard = new KpiCardPanel("GIÁ TB (ADR)", "0đ", "", false);
        revenueCard = new KpiCardPanel("DOANH THU", "0đ", "", true);

        // Define hierarchy styles
        totalRoomsCard.setNeutralStyle();
        occupancyRateCard.setInfoStyle();
        adrCard.setNeutralStyle();
        revenueCard.setPrimaryStyle();

        kpiPanel.add(totalRoomsCard);
        kpiPanel.add(occupancyRateCard);
        kpiPanel.add(adrCard);
        kpiPanel.add(revenueCard);

        return kpiPanel;
    }

    private JPanel createChartsSection() {
        JPanel chartsContainer = new JPanel(new BorderLayout(25, 0));
        chartsContainer.setBackground(new Color(248, 250, 252));

        // Let side: Occupancy Line Chart
        occupancyChart = new SimpleChartPanel("Công suất phòng 7 ngày qua (%)", SimpleChartPanel.ChartType.LINE);
        occupancyChart.setPreferredSize(new Dimension(0, 400));

        // Wrap to add some shadow/border effect feel
        JPanel leftWrapper = createChartWrapper(occupancyChart);
        leftWrapper.setPreferredSize(new Dimension(600, 0));
        chartsContainer.add(leftWrapper, BorderLayout.CENTER);

        // Right side: Room Current Status Donut Chart
        roomStatusChart = new SimpleChartPanel("Trạng thái phòng hiện tại", SimpleChartPanel.ChartType.DONUT);

        JPanel rightWrapper = createChartWrapper(roomStatusChart);
        rightWrapper.setPreferredSize(new Dimension(350, 0));
        chartsContainer.add(rightWrapper, BorderLayout.EAST);

        return chartsContainer;
    }

    private JPanel createChartWrapper(SimpleChartPanel chart) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        wrapper.add(chart, BorderLayout.CENTER);
        return wrapper;
    }

    public void loadRealData() {
        loadRealDataFiltered("Thống kê chung", "", "", "", "Tất cả");
    }

    public void loadRealDataFiltered(String thongKe, String tuan, String thang, String nam, String loaiPhong) {
        PhongService phongService = new PhongServiceImpl();
        HoaDonService hoaDonService = new HoaDonServiceImpl();

        List<PhongDTO> phongList = phongService.timTatCaPhongDTO();
        List<HoaDonDTO> hoaDonList = hoaDonService.getAllHoaDon();

        int targetYear = 0, targetMonth = 0, targetWeek = 0;
        try {
            if (nam != null && !nam.isEmpty())
                targetYear = Integer.parseInt(nam);
            if (thang != null && thang.startsWith("Tháng "))
                targetMonth = Integer.parseInt(thang.replace("Tháng ", ""));
            if (tuan != null && tuan.startsWith("Tuần "))
                targetWeek = Integer.parseInt(tuan.replace("Tuần ", ""));
        } catch (Exception ignored) {
        }

        // 1. Define Current Period
        LocalDate fStart = LocalDate.of(2000, 1, 1);
        LocalDate fEnd = LocalDate.now();

        if ("Năm".equals(thongKe)) {
            fStart = LocalDate.of(targetYear, 1, 1);
            fEnd = LocalDate.of(targetYear, 12, 31);
        } else if ("Tháng".equals(thongKe)) {
            fStart = LocalDate.of(targetYear, targetMonth, 1);
            fEnd = fStart.withDayOfMonth(fStart.lengthOfMonth());
        } else if ("Tuần".equals(thongKe)) {
            fStart = targetWeekStartDate(targetYear, targetMonth, targetWeek);
            fEnd = targetWeekEndDate(targetYear, targetMonth, targetWeek);
        } else if (!hoaDonList.isEmpty()) {
            // All-time: Start from the earliest possible date in the database (Check-in or
            // Invoice date)
            fStart = hoaDonList.stream()
                    .flatMap(h -> java.util.stream.Stream.of(h.getNgayXuatHD(), h.getNgayNhan()))
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.of(2000, 1, 1));
        }

        // 2. Define Previous Period
        LocalDate prevStart = null, prevEnd = null;
        String compStr = "so với kỳ trước";

        if ("Năm".equals(thongKe)) {
            prevStart = fStart.minusYears(1);
            prevEnd = fEnd.minusYears(1);
            compStr = "so với năm ngoái";
        } else if ("Tháng".equals(thongKe)) {
            prevStart = fStart.minusMonths(1);
            prevEnd = prevStart.withDayOfMonth(prevStart.lengthOfMonth());
            compStr = "so với tháng trước";
        } else if ("Tuần".equals(thongKe)) {
            compStr = "so với tuần trước";
            if (targetWeek > 1) {
                prevStart = targetWeekStartDate(targetYear, targetMonth, targetWeek - 1);
                prevEnd = targetWeekEndDate(targetYear, targetMonth, targetWeek - 1);
            } else if (fStart != null) {
                LocalDate lastMonth = fStart.minusMonths(1);
                prevStart = targetWeekStartDate(lastMonth.getYear(), lastMonth.getMonthValue(), 5);
                prevEnd = targetWeekEndDate(lastMonth.getYear(), lastMonth.getMonthValue(), 5);
                if (prevStart == null) {
                    prevStart = targetWeekStartDate(lastMonth.getYear(), lastMonth.getMonthValue(), 4);
                    prevEnd = targetWeekEndDate(lastMonth.getYear(), lastMonth.getMonthValue(), 4);
                }
            }
        }

        // 3. Filter Rooms for Capacity
        List<PhongDTO> filteredRooms = phongList;
        if (loaiPhong != null && !"Tất cả".equals(loaiPhong)) {
            filteredRooms = phongList.stream().filter(p -> loaiPhong.equalsIgnoreCase(p.getTenLoaiPhong()))
                    .collect(java.util.stream.Collectors.toList());
        }
        int totalRooms = filteredRooms.size();

        // 4. Calculate Stats
        List<HoaDonDTO> finalHoaDonList = hoaDonList;
        if (loaiPhong != null && !"Tất cả".equals(loaiPhong)) {
            finalHoaDonList = hoaDonList.stream()
                    .filter(h -> loaiPhong.equalsIgnoreCase(h.getTenLoaiPhong()))
                    .collect(java.util.stream.Collectors.toList());
        }

        Stats currentStats = calculateStats(finalHoaDonList, fStart, fEnd, totalRooms);

        // Filter for Previous Period as well (for accurate comparison)
        List<HoaDonDTO> finalPrevHoaDonList = finalHoaDonList; // If already filtered, use it.
        // Actually, if we filter the source list 'hoaDonList' once, we can reuse it.

        Stats prevStats = calculateStats(finalHoaDonList, prevStart, prevEnd, totalRooms);

        // 5. Update KPI Cards
        DecimalFormat dfMoney = new DecimalFormat("#,###đ");
        DecimalFormat dfPercent = new DecimalFormat("0.##'%'");
        DecimalFormat dfNum = new DecimalFormat("#,###");

        String occTitle = "TỈ LỆ ĐẶT PHÒNG";
        String occValue = dfPercent.format(currentStats.occupancy);
        String occChangeStr = "";
        boolean occPositive = true;

        String revTitle = "DOANH THU KỲ NÀY";
        String revValue = dfMoney.format(currentStats.totalRevenue);
        String revChangeStr = "";
        boolean revPositive = true;

        if ("Thống kê chung".equals(thongKe)) {
            occTitle = "TỔNG ĐÊM PHÒNG";
            occValue = dfNum.format(currentStats.sold);
            revTitle = "TỔNG DOANH THU";
        } else {
            double diffOcc = currentStats.occupancy - prevStats.occupancy;
            occPositive = diffOcc >= 0;
            occChangeStr = String.format("%s%.2f%% %s", diffOcc > 0 ? "+" : "", diffOcc, compStr);

            double diffRevPct = 0;
            if (prevStats.totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
                if (currentStats.totalRevenue.compareTo(BigDecimal.ZERO) > 0)
                    diffRevPct = 100.0;
            } else {
                diffRevPct = (currentStats.totalRevenue.doubleValue() - prevStats.totalRevenue.doubleValue())
                        / prevStats.totalRevenue.doubleValue() * 100.0;
            }
            revPositive = diffRevPct >= 0;
            revChangeStr = String.format("%s%.2f%% %s", diffRevPct > 0 ? "+" : "", diffRevPct, compStr);
        }

        totalRoomsCard.updateData("TỔNG SỐ PHÒNG", dfNum.format(totalRooms), "Thực tế", true);
        occupancyRateCard.updateData(occTitle, occValue, occChangeStr, occPositive);
        adrCard.updateData("GIÁ TB (ADR)", dfMoney.format(currentStats.adr), "", false);
        revenueCard.updateData(revTitle, revValue, revChangeStr, revPositive);

        // 6. Update Charts
        updateOccupancyChart(thongKe, targetYear, targetMonth, targetWeek, hoaDonList, totalRooms);

        int occupiedCount = 0, availableCount = 0, maintenanceCount = 0;
        for (PhongDTO p : filteredRooms) {
            String status = p.getTrangThaiHienThi();
            if ("DANG_O".equals(status))
                occupiedCount++;
            else if ("SAN_SANG".equals(status))
                availableCount++;
            else
                maintenanceCount++;
        }
        Map<String, Double> donutData = new LinkedHashMap<>();
        donutData.put("Đang sử dụng", (double) occupiedCount);
        donutData.put("Trống", (double) availableCount);
        donutData.put("Bảo trì / Dọn", (double) maintenanceCount);
        roomStatusChart.setData(donutData);
    }

    private void updateOccupancyChart(String mode, int year, int month, int week, List<HoaDonDTO> invoices,
            int totalRooms) {
        occupancyChart.setYAxisPercentage(true);
        Map<String, Double> lineData = new LinkedHashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        if ("Tháng".equals(mode)) {
            for (int w = 1; w <= 5; w++) {
                LocalDate ws = targetWeekStartDate(year, month, w);
                LocalDate we = targetWeekEndDate(year, month, w);
                if (ws == null)
                    continue;
                lineData.put("T." + w, calculateStats(invoices, ws, we, totalRooms).occupancy);
            }
            occupancyChart.setTitle("Tỉ lệ đặt phòng tháng " + month + "/" + year);
        } else if ("Năm".equals(mode)) {
            for (int m = 1; m <= 12; m++) {
                LocalDate ms = LocalDate.of(year, m, 1);
                LocalDate me = ms.withDayOfMonth(ms.lengthOfMonth());
                lineData.put("T." + m, calculateStats(invoices, ms, me, totalRooms).occupancy);
            }
            occupancyChart.setTitle("Tỉ lệ đặt phòng Năm " + year);
        } else if ("Tuần".equals(mode)) {
            LocalDate ws = targetWeekStartDate(year, month, week);
            LocalDate we = targetWeekEndDate(year, month, week);
            if (ws != null && we != null) {
                LocalDate cur = ws;
                while (!cur.isAfter(we)) {
                    lineData.put(cur.format(dtf), calculateStats(invoices, cur, cur, totalRooms).occupancy);
                    cur = cur.plusDays(1);
                }
            }
            occupancyChart.setTitle("Tỉ lệ đặt phòng theo ngày - Tuần " + week);
        } else {
            // All-time trend: If data covers 1 year or less, show by Month. Otherwise show
            // by Year.
            int startYear = invoices.stream()
                    .filter(h -> h.getNgayXuatHD() != null)
                    .mapToInt(h -> h.getNgayXuatHD().getYear())
                    .min()
                    .orElse(LocalDate.now().getYear());

            int currentYear = LocalDate.now().getYear();

            if (startYear == currentYear) {
                // Only this year? Show all months of this year for trend
                for (int m = 1; m <= 12; m++) {
                    LocalDate ms = LocalDate.of(startYear, m, 1);
                    LocalDate me = ms.withDayOfMonth(ms.lengthOfMonth());
                    lineData.put("Th." + m, calculateStats(invoices, ms, me, totalRooms).occupancy);
                }
                occupancyChart.setTitle("Xu hướng công suất phòng năm " + startYear);
            } else {
                // Multiple years? Show yearly breakdown
                for (int y = startYear; y <= currentYear; y++) {
                    LocalDate ys = LocalDate.of(y, 1, 1);
                    LocalDate ye = LocalDate.of(y, 12, 31);
                    lineData.put(String.valueOf(y), calculateStats(invoices, ys, ye, totalRooms).occupancy);
                }
                occupancyChart.setTitle("Xu hướng công suất phòng qua các năm");
            }
        }

        if (lineData.size() <= 1)
            occupancyChart.setChartType(SimpleChartPanel.ChartType.BAR);
        else
            occupancyChart.setChartType(SimpleChartPanel.ChartType.LINE);
        occupancyChart.setData(lineData);
    }

    private static class Stats {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal roomRevenue = BigDecimal.ZERO;
        long sold = 0;
        double occupancy = 0;
        double adr = 0;
    }

    private Stats calculateStats(List<HoaDonDTO> invoices, LocalDate start, LocalDate end, int totalRooms) {
        Stats s = new Stats();
        if (start == null || end == null || start.isAfter(end) || totalRooms <= 0)
            return s;

        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        long availableRoomNights = (long) totalRooms * days;

        for (HoaDonDTO hd : invoices) {
            // Get Check-in/out dates
            LocalDate checkIn = hd.getNgayNhan() != null ? hd.getNgayNhan().toLocalDate()
                    : (hd.getNgayXuatHD() != null ? hd.getNgayXuatHD().toLocalDate() : null);
            LocalDate checkOut = hd.getNgayTra() != null ? hd.getNgayTra().toLocalDate()
                    : (hd.getNgayXuatHD() != null ? hd.getNgayXuatHD().toLocalDate() : null);

            if (checkIn != null && checkOut != null) {
                // 1. Calculate Stay Nights
                long totalStayNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                if (totalStayNights < 1 && checkIn.isEqual(checkOut))
                    totalStayNights = 1;

                if (totalStayNights > 0) {
                    // 2. Room Nights within the filtered period [start, end]
                    LocalDate stayStart = checkIn;
                    LocalDate stayEnd = checkOut.isEqual(checkIn) ? checkIn : checkOut.minusDays(1);
                    if (stayEnd.isBefore(stayStart))
                        stayEnd = stayStart;

                    LocalDate overlapS = stayStart.isBefore(start) ? start : stayStart;
                    LocalDate overlapE = stayEnd.isAfter(end) ? end : stayEnd;

                    if (!overlapS.isAfter(overlapE)) {
                        long nightsInPeriod = java.time.temporal.ChronoUnit.DAYS.between(overlapS, overlapE) + 1;
                        s.sold += nightsInPeriod;

                        // 3. Earned Revenue Calculation (Prorated)
                        BigDecimal dailyRoom = (hd.getTongTienPhong() != null) ? hd.getTongTienPhong().divide(
                                new BigDecimal(totalStayNights), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal dailyDiscount = (hd.getGiamGia() != null) ? hd.getGiamGia().divide(
                                new BigDecimal(totalStayNights), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

                        BigDecimal earnedRoom = dailyRoom.multiply(new BigDecimal(nightsInPeriod));
                        BigDecimal earnedDisc = dailyDiscount.multiply(new BigDecimal(nightsInPeriod));

                        s.roomRevenue = s.roomRevenue.add(earnedRoom);
                        s.totalRevenue = s.totalRevenue.add(earnedRoom).subtract(earnedDisc);
                    }
                }
            }

            // 4. Service Revenue: Counted on checkout date (Invoice Export Date)
            if (hd.getNgayXuatHD() != null) {
                LocalDate exp = hd.getNgayXuatHD().toLocalDate();
                if (!exp.isBefore(start) && !exp.isAfter(end)) {
                    if (hd.getTongTienDichVu() != null)
                        s.totalRevenue = s.totalRevenue.add(hd.getTongTienDichVu());
                }
            }
        }

        if (availableRoomNights > 0)
            s.occupancy = (double) s.sold / availableRoomNights * 100.0;
        if (s.sold > 0)
            s.adr = s.roomRevenue.doubleValue() / s.sold;
        return s;
    }

    private LocalDate targetWeekStartDate(int year, int month, int week) {
        LocalDate first = LocalDate.of(year, month, 1);
        if (week == 5) {
            if (first.lengthOfMonth() < 29)
                return null;
            return LocalDate.of(year, month, 29);
        }
        return first.plusDays((week - 1) * 7);
    }

    private LocalDate targetWeekEndDate(int year, int month, int week) {
        LocalDate first = LocalDate.of(year, month, 1);
        if (week == 5) {
            if (first.lengthOfMonth() < 29)
                return null;
            return first.withDayOfMonth(first.lengthOfMonth());
        }
        LocalDate end = first.plusDays((week - 1) * 7 + 6);
        if (end.getDayOfMonth() > 28)
            end = first.withDayOfMonth(28);
        return end;
    }
}
