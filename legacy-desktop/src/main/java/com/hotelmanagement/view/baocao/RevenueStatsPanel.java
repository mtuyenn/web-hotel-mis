package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.hotelmanagement.controller.DatPhongController;
import com.hotelmanagement.controller.HoaDonController;
import com.hotelmanagement.model.dto.ChiTietDatPhongDTO;
import com.hotelmanagement.model.dto.DatPhongDTO;
import com.hotelmanagement.model.dto.HoaDonDTO;

/**
 * Lớp RevenueStatsPanel - Panel hiển thị thống kê doanh thu chuyên sâu
 * 
 * Chức năng:
 * - Hiển thị biểu đồ doanh thu theo thời gian (line chart)
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
public class RevenueStatsPanel extends JPanel {

    private final HoaDonController hoaDonController = new HoaDonController();
    private final DatPhongController datPhongController = new DatPhongController();

    private final FilterDashboardPanel filterPanel;
    private final SimpleChartPanel revenueByRoomTypeChart;
    private final SimpleChartPanel revenueTrendChart;

    public RevenueStatsPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // Filter
        filterPanel = new FilterDashboardPanel(this::loadData);
        add(filterPanel, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        revenueTrendChart = new SimpleChartPanel("XU HƯỚNG DOANH THU THEO THỜI GIAN", SimpleChartPanel.ChartType.LINE);
        revenueTrendChart.setPreferredSize(new Dimension(0, 380));
        revenueTrendChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        revenueByRoomTypeChart = new SimpleChartPanel("DOANH THU THEO LOẠI PHÒNG", SimpleChartPanel.ChartType.BAR);
        revenueByRoomTypeChart.setPreferredSize(new Dimension(0, 380));
        revenueByRoomTypeChart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        content.add(revenueTrendChart);
        content.add(Box.createVerticalStrut(25));
        content.add(revenueByRoomTypeChart);

        add(content, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        LocalDate start = filterPanel.getStartDate();
        LocalDate end = filterPanel.getEndDate();

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() {
                Map<String, Object> results = new HashMap<>();
                List<HoaDonDTO> hds = hoaDonController.getAllHoaDon();
                List<DatPhongDTO> dps = datPhongController.getAllDatPhong();

                // 1. Revenue Trend (Daily or Monthly)
                Map<String, Double> trendData = new LinkedHashMap<>();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");

                boolean isYearly = filterPanel.getMode() == FilterDashboardPanel.FilterMode.YEAR;
                boolean isDaily = filterPanel.getMode() == FilterDashboardPanel.FilterMode.DAY;

                if (isDaily) {
                    double dailySum = 0;
                    for (HoaDonDTO hd : hds) {
                        if (hd.getNgayXuatHD() == null)
                            continue;
                        if (hd.getNgayXuatHD().toLocalDate().equals(start)) {
                            dailySum += hd.getTongTienPhaiTra().doubleValue();
                        }
                    }
                    trendData.put(start.format(df), dailySum);
                } else if (isYearly) {
                    for (int month = 1; month <= 12; month++) {
                        double monthlySum = 0;
                        for (HoaDonDTO hd : hds) {
                            if (hd.getNgayXuatHD() == null)
                                continue;
                            LocalDate d = hd.getNgayXuatHD().toLocalDate();
                            if (d.getYear() == start.getYear() && d.getMonthValue() == month) {
                                monthlySum += hd.getTongTienPhaiTra().doubleValue();
                            }
                        }
                        trendData.put("Th " + month, monthlySum);
                    }
                } else {
                    LocalDate current = start;
                    while (!current.isAfter(end)) {
                        double dailySum = 0;
                        for (HoaDonDTO hd : hds) {
                            if (hd.getNgayXuatHD() == null)
                                continue;
                            if (hd.getNgayXuatHD().toLocalDate().equals(current)) {
                                dailySum += hd.getTongTienPhaiTra().doubleValue();
                            }
                        }
                        trendData.put(current.format(df), dailySum);
                        current = current.plusDays(1);
                        if (trendData.size() > 60)
                            break;
                    }
                }
                results.put("trend", trendData);

                // 2. Revenue by Room Type
                Map<String, Double> typeData = new HashMap<>();
                for (HoaDonDTO hd : hds) {
                    if (hd.getNgayXuatHD() == null)
                        continue;
                    LocalDate d = hd.getNgayXuatHD().toLocalDate();
                    if (d.isBefore(start) || d.isAfter(end))
                        continue;

                    Optional<DatPhongDTO> dpOpt = dps.stream()
                            .filter(dp -> dp.getMaDP().equals(hd.getMaDP()))
                            .findFirst();

                    if (dpOpt.isPresent()) {
                        DatPhongDTO dp = dpOpt.get();
                        if (dp.getChiTietDatPhongs() != null) {
                            for (ChiTietDatPhongDTO ct : dp.getChiTietDatPhongs()) {
                                String typeName = ct.getTenLoaiPhong();
                                if (typeName == null)
                                    typeName = "Khác";
                                typeData.put(typeName, typeData.getOrDefault(typeName, 0.0)
                                        + hd.getTongTienPhaiTra().doubleValue() / dp.getChiTietDatPhongs().size());
                            }
                        }
                    }
                }
                results.put("byType", typeData);

                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Map<String, Object> r = get();
                    revenueTrendChart.setData((Map<String, Double>) r.get("trend"));
                    revenueByRoomTypeChart.setData((Map<String, Double>) r.get("byType"));
                } catch (Exception e) {
                    System.err.println("Lỗi tải Revenue: " + e.getMessage());
                }
            }
        }.execute();
    }
}
