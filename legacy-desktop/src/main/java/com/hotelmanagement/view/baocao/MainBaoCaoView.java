package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Lớp MainBaoCaoView - Màn hình chính báo cáo và thống kê
 * 
 * Chức năng:
 * - Quản lý navigation giữa các phân hệ báo cáo: Tổng quan và Thống kê doanh
 * thu
 * - Hiển thị bộ lọc thời gian cho báo cáo
 * - Tự động tải dữ liệu khi chuyển đổi phân hệ
 * 
 * Thiết kế:
 * - Giao diện chia thành 2 phần: Thanh điều hướng (trên) và Nội dung (dưới)
 * - Sử dụng CardLayout để quản lý các phân hệ báo cáo
 * - Sử dụng FlowLayout cho thanh điều hướng và bộ lọc
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị bộ lọc theo chế độ thống kê
 */
public class MainBaoCaoView extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final Map<String, JButton> subButtons = new HashMap<>();
    private JButton activeButton = null;

    private OverviewPanel overviewPanel;
    private RevenuePanel revenuePanel;

    private final Color SUB_NAV_BG = new Color(248, 250, 252);
    private final Color GOLD_ACCENT = new Color(212, 175, 55);

    public MainBaoCaoView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header Sub-Nav
        JPanel subNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        subNav.setBackground(SUB_NAV_BG);
        subNav.setPreferredSize(new Dimension(0, 60));
        subNav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        String[][] tabs = {
                { "TỔNG QUAN", "overview" },
                { "THỐNG KÊ DOANH THU", "revenue" }
        };

        for (String[] tab : tabs) {
            JButton btn = createSubNavButton(tab[0], tab[1]);
            subNav.add(btn);
            subButtons.put(tab[1], btn);
            if (activeButton == null) {
                activeButton = btn;
                btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, GOLD_ACCENT));
                btn.setForeground(new Color(15, 23, 42));
            }
        }

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(subNav, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        northPanel.add(filterPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        // Content
        overviewPanel = new OverviewPanel();
        revenuePanel = new RevenuePanel();

        container.add(overviewPanel, "overview");
        container.add(revenuePanel, "revenue");

        add(container, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240))); // Slate 200

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(71, 85, 105);

        // Time Period Combo
        JLabel lblTime = new JLabel("Thống kê:");
        lblTime.setFont(labelFont);
        lblTime.setForeground(labelColor);
        JComboBox<String> cboThongKe = new JComboBox<>(new String[] { "Thống kê chung", "Tuần", "Tháng", "Năm" });
        cboThongKe.setPreferredSize(new Dimension(130, 30));
        cboThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboThongKe.setBackground(Color.WHITE);

        // Detailed combos
        JComboBox<String> cboTuan = new JComboBox<>(new String[] { "Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5" });
        JComboBox<String> cboThang = new JComboBox<>(new String[] { "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
                "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12" });

        int currentYear = java.time.LocalDate.now().getYear();
        JComboBox<String> cboNam = new JComboBox<>();
        for (int y = currentYear - 10; y <= currentYear; y++) {
            cboNam.addItem(String.valueOf(y));
        }
        cboNam.setSelectedItem(String.valueOf(currentYear));

        Dimension cboDim = new Dimension(90, 30);
        cboTuan.setPreferredSize(cboDim);
        cboTuan.setBackground(Color.WHITE);
        cboThang.setPreferredSize(cboDim);
        cboThang.setBackground(Color.WHITE);
        cboNam.setPreferredSize(cboDim);
        cboNam.setBackground(Color.WHITE);

        // Logic to toggle visibility
        Runnable updateVis = () -> {
            String sel = (String) cboThongKe.getSelectedItem();
            if ("Thống kê chung".equals(sel)) {
                cboTuan.setVisible(false);
                cboThang.setVisible(false);
                cboNam.setVisible(false);
            } else if ("Năm".equals(sel)) {
                cboTuan.setVisible(false);
                cboThang.setVisible(false);
                cboNam.setVisible(true);
            } else if ("Tháng".equals(sel)) {
                cboTuan.setVisible(false);
                cboThang.setVisible(true);
                cboNam.setVisible(true);
            } else { // Tuần
                cboTuan.setVisible(true);
                cboThang.setVisible(true);
                cboNam.setVisible(true);
            }
            filterPanel.revalidate();
            filterPanel.repaint();
        };

        cboThongKe.addActionListener(e -> updateVis.run());
        updateVis.run();

        // Room Type Combo
        JLabel lblRoom = new JLabel("Loại phòng:");
        lblRoom.setFont(labelFont);
        lblRoom.setForeground(labelColor);
        JComboBox<String> cboRoom = new JComboBox<>(
                new String[] { "Tất cả", "Phòng Đơn", "Phòng Đôi", "Phòng VIP", "Phòng Gia Đình" });
        cboRoom.setPreferredSize(new Dimension(140, 30));
        cboRoom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboRoom.setBackground(Color.WHITE);

        // Apply Button
        JButton btnApply = new JButton("Áp dụng");
        btnApply.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnApply.setBackground(new Color(15, 23, 42)); // Slate 900
        btnApply.setForeground(Color.BLACK);
        btnApply.setFocusPainted(false);
        btnApply.setPreferredSize(new Dimension(100, 30));

        btnApply.addActionListener(e -> {
            String thongKe = (String) cboThongKe.getSelectedItem();
            String tuan = (String) cboTuan.getSelectedItem();
            String thang = (String) cboThang.getSelectedItem();
            String nam = (String) cboNam.getSelectedItem();
            String loaiPhong = (String) cboRoom.getSelectedItem();

            if (overviewPanel != null) {
                overviewPanel.loadRealDataFiltered(thongKe, tuan, thang, nam, loaiPhong);
            }
            if (revenuePanel != null) {
                revenuePanel.loadRealDataFiltered(thongKe, tuan, thang, nam, loaiPhong);
            }
        });

        filterPanel.add(lblTime);
        filterPanel.add(cboThongKe);
        filterPanel.add(cboTuan);
        filterPanel.add(cboThang);
        filterPanel.add(cboNam);
        filterPanel.add(lblRoom);
        filterPanel.add(cboRoom);
        filterPanel.add(btnApply);

        return filterPanel;
    }

    private JButton createSubNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(100, 116, 139));
        btn.setBackground(SUB_NAV_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 60));

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBorder(null);
                activeButton.setForeground(new Color(100, 116, 139));
            }
            activeButton = btn;
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, GOLD_ACCENT));
            btn.setForeground(new Color(15, 23, 42));
            cardLayout.show(container, cardName);
        });

        return btn;
    }
}
