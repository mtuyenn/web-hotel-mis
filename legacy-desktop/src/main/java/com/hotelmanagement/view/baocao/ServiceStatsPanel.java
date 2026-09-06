package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.hotelmanagement.controller.DatPhongController;
import com.hotelmanagement.model.dto.ChiTietDichVuDTO;
import com.hotelmanagement.model.dto.DatPhongDTO;

/**
 * Lớp ServiceStatsPanel - Panel hiển thị thống kê dịch vụ
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
public class ServiceStatsPanel extends JPanel {

    private final DatPhongController datPhongController = new DatPhongController();
    private final SimpleChartPanel barChart;
    private final JTable table;
    private final DefaultTableModel model;
    private final JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private int currentFilter = 0; // 0: Ngày, 1: Tháng, 2: Năm

    public ServiceStatsPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 30, 30, 30));

        // Filter
        filterPanel.setOpaque(false);
        String[] filters = { "7 Ngày gần nhất", "Tháng này", "Năm nay" };
        for (int i = 0; i < filters.length; i++) {
            filterPanel.add(createFilterButton(filters[i], i));
        }
        add(filterPanel, BorderLayout.NORTH);

        // Chart
        barChart = new SimpleChartPanel("SƠ ĐỒ CỘT TOP DỊCH VỤ", SimpleChartPanel.ChartType.BAR);
        barChart.setPreferredSize(new Dimension(0, 300));

        // Table
        String[] cols = { "Tên Dịch Vụ", "Số lần sử dụng", "Tổng số tiền" };
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane scroll = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(barChart, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    private JButton createFilterButton(String text, int index) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(140, 35));
        btn.setBackground(index == currentFilter ? new Color(52, 152, 219) : Color.WHITE);
        btn.setForeground(index == currentFilter ? Color.WHITE : Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            currentFilter = index;
            updateButtonStyles();
            loadData();
        });
        return btn;
    }

    private void updateButtonStyles() {
        for (int i = 0; i < filterPanel.getComponentCount(); i++) {
            JButton b = (JButton) filterPanel.getComponent(i);
            b.setBackground(i == currentFilter ? new Color(52, 152, 219) : Color.WHITE);
            b.setForeground(i == currentFilter ? Color.WHITE : Color.DARK_GRAY);
        }
    }

    private void loadData() {
        new SwingWorker<Map<String, ServiceStat>, Void>() {
            @Override
            protected Map<String, ServiceStat> doInBackground() {
                List<DatPhongDTO> allDps = datPhongController.getAllDatPhong();
                Map<String, ServiceStat> stats = new HashMap<>();
                LocalDate now = LocalDate.now();

                for (DatPhongDTO dp : allDps) {
                    if (dp.getChiTietDichVus() != null) {
                        for (ChiTietDichVuDTO ct : dp.getChiTietDichVus()) {
                            if (ct.getNgaySuDung() == null)
                                continue;

                            boolean match = false;
                            if (currentFilter == 0) { // 7 ngày
                                if (!ct.getNgaySuDung().isBefore(now.minusDays(7)))
                                    match = true;
                            } else if (currentFilter == 1) { // Tháng
                                if (ct.getNgaySuDung().getMonth() == now.getMonth()
                                        && ct.getNgaySuDung().getYear() == now.getYear())
                                    match = true;
                            } else { // Năm
                                if (ct.getNgaySuDung().getYear() == now.getYear())
                                    match = true;
                            }

                            if (match) {
                                String name = ct.getTenDichVu();
                                if (name == null)
                                    continue;
                                ServiceStat s = stats.getOrDefault(name, new ServiceStat(name));
                                s.count += ct.getSoLuong() != null ? ct.getSoLuong() : 0;
                                s.revenue += ct.getThanhTien() != null ? ct.getThanhTien().doubleValue() : 0;
                                stats.put(name, s);
                            }
                        }
                    }
                }
                return stats;
            }

            @Override
            protected void done() {
                try {
                    Map<String, ServiceStat> result = get();

                    // Top 5 Chart
                    Map<String, Double> chartData = result.values().stream()
                            .sorted((s1, s2) -> Integer.compare(s2.count, s1.count))
                            .limit(5)
                            .collect(Collectors.toMap(
                                    s -> s.name,
                                    s -> (double) s.count,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new));
                    barChart.setData(chartData);

                    // Table
                    model.setRowCount(0);
                    result.values().stream()
                            .sorted((s1, s2) -> Double.compare(s2.revenue, s1.revenue))
                            .forEach(s -> model.addRow(new Object[] {
                                    s.name, s.count, String.format("%,.0f VND", s.revenue)
                            }));
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    static class ServiceStat {
        String name;
        int count = 0;
        double revenue = 0;

        ServiceStat(String name) {
            this.name = name;
        }
    }
}
