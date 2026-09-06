package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.hotelmanagement.service.HoaDonService;
import com.hotelmanagement.service.impl.HoaDonServiceImpl;
import com.hotelmanagement.model.dto.HoaDonDTO;

/**
 * Lớp RevenuePanel - Panel hiển thị báo cáo doanh thu
 * 
 * Chức năng:
 * - Hiển thị biểu đồ doanh thu theo thời gian (area chart)
 * - Hiển thị biểu đồ doanh thu theo loại phòng (bar chart)
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * - Hỗ trợ lọc dữ liệu theo thời gian (ngày, tháng, năm)
 * 
 * Thiết kế:
 * - Giao diện chia thành 2 phần: Biểu đồ doanh thu theo thời gian (trên) và
 * Biểu đồ doanh thu theo loại phòng (dưới)
 * - Sử dụng BorderLayout để sắp xếp các thành phần
 * - Sử dụng GridBagLayout để sắp xếp các biểu đồ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class RevenuePanel extends JPanel {

    private SimpleChartPanel revenueChart;
    private SimpleChartPanel roomTypeRevenueChart;

    public RevenuePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252)); // Slate 50

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(new Color(248, 250, 252));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 25, 0); // Bottom margin 25

        // 1. Top Chart - Area Chart for Revenue Over Time
        revenueChart = new SimpleChartPanel("Doanh thu 7 ngày qua (VNĐ)", SimpleChartPanel.ChartType.AREA);
        JPanel topWrapper = createChartWrapper(revenueChart);
        topWrapper.setPreferredSize(new Dimension(0, 350));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.6;
        contentPanel.add(topWrapper, gbc);

        // 2. Bottom Chart - Bar Chart for Revenue by Room Type
        roomTypeRevenueChart = new SimpleChartPanel("Doanh thu theo Loại Phòng (VNĐ)", SimpleChartPanel.ChartType.BAR);
        JPanel bottomWrapper = createChartWrapper(roomTypeRevenueChart);
        bottomWrapper.setPreferredSize(new Dimension(0, 300));

        gbc.gridy = 1;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(bottomWrapper, gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        loadRealDataFiltered("Thống kê chung", "", "", "", "Tất cả");
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

    public void loadRealDataFiltered(String thongKe, String tuan, String thang, String nam, String loaiPhong) {
        HoaDonService hoaDonService = new HoaDonServiceImpl();
        List<HoaDonDTO> hoaDonList = hoaDonService.getAllHoaDon();

        // 1. Phân tích Filter
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
        final int fYear = targetYear;
        final int fMonth = targetMonth;
        final int fWeek = targetWeek;

        // 2. Room Type & Date Range Filter
        List<HoaDonDTO> baseList = hoaDonList;
        if (loaiPhong != null && !"Tất cả".equals(loaiPhong)) {
            baseList = hoaDonList.stream()
                    .filter(hd -> loaiPhong.equalsIgnoreCase(hd.getTenLoaiPhong()))
                    .collect(java.util.stream.Collectors.toList());
        }

        LocalDate fStart, fEnd;
        if ("Năm".equals(thongKe)) {
            fStart = LocalDate.of(fYear, 1, 1);
            fEnd = LocalDate.of(fYear, 12, 31);
        } else if ("Tháng".equals(thongKe)) {
            fStart = LocalDate.of(fYear, fMonth, 1);
            fEnd = fStart.plusMonths(1).minusDays(1);
        } else if ("Tuần".equals(thongKe)) {
            // Re-using logic from OverviewPanel for week boundaries
            fStart = LocalDate.of(fYear, fMonth, (fWeek - 1) * 7 + 1);
            if (fWeek == 5)
                fEnd = fStart.plusMonths(1).minusDays(1);
            else
                fEnd = fStart.plusDays(6);
        } else {
            // All-time: find earliest date
            fStart = hoaDonList.stream()
                    .flatMap(h -> java.util.stream.Stream.of(h.getNgayXuatHD(), h.getNgayNhan()))
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.of(2000, 1, 1));
            fEnd = LocalDate.now();
        }

        // 3. Chart Data Generation (Using Earned Revenue for sub-periods)
        Map<String, Double> areaData = new java.util.LinkedHashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

        if ("Tháng".equals(thongKe)) {
            for (int w = 1; w <= 5; w++) {
                LocalDate ws = LocalDate.of(fYear, fMonth, (w - 1) * 7 + 1);
                LocalDate we = (w == 5) ? ws.plusMonths(1).minusDays(ws.getDayOfMonth()) : ws.plusDays(6);
                if (ws.getMonthValue() != fMonth)
                    break;
                areaData.put("Tuần " + w, calculateEarnedRevenue(baseList, ws, we));
            }
            revenueChart.setTitle("Doanh thu tháng " + fMonth + "/" + fYear + " (VNĐ)");
        } else if ("Năm".equals(thongKe)) {
            for (int m = 1; m <= 12; m++) {
                LocalDate ms = LocalDate.of(fYear, m, 1);
                LocalDate me = ms.plusMonths(1).minusDays(1);
                areaData.put("Th." + m, calculateEarnedRevenue(baseList, ms, me));
            }
            revenueChart.setTitle("Doanh thu Năm " + fYear + " (VNĐ)");
        } else if ("Tuần".equals(thongKe)) {
            for (int i = 0; i < 7; i++) {
                LocalDate d = fStart.plusDays(i);
                if (d.getMonthValue() != fMonth)
                    break;
                areaData.put(d.format(dtf), calculateEarnedRevenue(baseList, d, d));
            }
            revenueChart.setTitle("Doanh thu Tuần " + fWeek + " (VNĐ)");
        } else {
            int startY = fStart.getYear();
            int endY = LocalDate.now().getYear();
            for (int y = startY; y <= endY; y++) {
                LocalDate ys = LocalDate.of(y, 1, 1);
                LocalDate ye = LocalDate.of(y, 12, 31);
                areaData.put(String.valueOf(y), calculateEarnedRevenue(baseList, ys, ye));
            }
            revenueChart.setTitle("Tổng Doanh Thu Qua Các Năm (VNĐ)");
        }

        revenueChart.setData(areaData);
        if (areaData.size() <= 1) {
            revenueChart.setChartType(SimpleChartPanel.ChartType.BAR);
        } else {
            revenueChart.setChartType(SimpleChartPanel.ChartType.AREA);
        }

        // 4. Bar chart data: Group by Real Room Type
        Map<String, Double> barData = new java.util.LinkedHashMap<>();
        for (HoaDonDTO hd : baseList) {
            String type = hd.getTenLoaiPhong();
            if (type == null || type.isEmpty())
                type = "Chưa phân loại";
            double val = calculateEarnedRevenue(java.util.Collections.singletonList(hd), fStart, fEnd);
            barData.put(type, barData.getOrDefault(type, 0.0) + val);
        }

        roomTypeRevenueChart.setData(barData);
        roomTypeRevenueChart.setTitle("Doanh thu theo Loại Phòng (Thực tế)");
    }

    private double calculateEarnedRevenue(List<HoaDonDTO> invoices, LocalDate start, LocalDate end) {
        BigDecimal total = BigDecimal.ZERO;
        for (HoaDonDTO hd : invoices) {
            LocalDate checkIn = hd.getNgayNhan() != null ? hd.getNgayNhan().toLocalDate()
                    : (hd.getNgayXuatHD() != null ? hd.getNgayXuatHD().toLocalDate() : null);
            LocalDate checkOut = hd.getNgayTra() != null ? hd.getNgayTra().toLocalDate()
                    : (hd.getNgayXuatHD() != null ? hd.getNgayXuatHD().toLocalDate() : null);

            if (checkIn != null && checkOut != null) {
                long totalStayNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                if (totalStayNights < 1 && checkIn.isEqual(checkOut))
                    totalStayNights = 1;

                if (totalStayNights > 0) {
                    LocalDate stayStart = checkIn;
                    LocalDate stayEnd = checkOut.isEqual(checkIn) ? checkIn : checkOut.minusDays(1);
                    if (stayEnd.isBefore(stayStart))
                        stayEnd = stayStart;

                    LocalDate overlapS = stayStart.isBefore(start) ? start : stayStart;
                    LocalDate overlapE = stayEnd.isAfter(end) ? end : stayEnd;

                    if (!overlapS.isAfter(overlapE)) {
                        long nightsInPeriod = java.time.temporal.ChronoUnit.DAYS.between(overlapS, overlapE) + 1;
                        BigDecimal dailyRoom = (hd.getTongTienPhong() != null) ? hd.getTongTienPhong().divide(
                                new BigDecimal(totalStayNights), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
                        BigDecimal dailyDisc = (hd.getGiamGia() != null) ? hd.getGiamGia().divide(
                                new BigDecimal(totalStayNights), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

                        total = total.add((dailyRoom.subtract(dailyDisc)).multiply(new BigDecimal(nightsInPeriod)));
                    }
                }
            }
            // Service counts on checkout date
            if (hd.getNgayXuatHD() != null) {
                LocalDate exp = hd.getNgayXuatHD().toLocalDate();
                if (!exp.isBefore(start) && !exp.isAfter(end)) {
                    if (hd.getTongTienDichVu() != null)
                        total = total.add(hd.getTongTienDichVu());
                }
            }
        }
        return total.doubleValue();
    }
}
