package com.hotelmanagement.view;

import com.hotelmanagement.view.hoadon.HoaDonView;
import com.hotelmanagement.view.baocao.MainBaoCaoView;
import com.hotelmanagement.view.dichvu.DichVuView;
import com.hotelmanagement.view.khachhang.KhachHangView;
import com.hotelmanagement.view.phong.MainRoomDashboardView;
import com.hotelmanagement.view.nhanvien.NhanVienView;

import javax.swing.*;
import java.awt.*;

/**
 * MAIN FRAME - Thiết kế mới với Sidebar trái + CardLayout
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private MainRoomDashboardView roomView;

    public MainFrame(String tenNhanVien, String chucVu) {
        setTitle("HỆ THỐNG QUẢN LÝ KHÁCH SẠN - " + tenNhanVien + " (" + chucVu + ")");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        initComponents(tenNhanVien, chucVu);
    }

    private void initComponents(String tenNhanVien, String chucVu) {
        // Sidebar trái
        SidebarMenu sidebar = new SidebarMenu(cardLayout, contentPanel, tenNhanVien);

        // Các view
        roomView = new MainRoomDashboardView();
        contentPanel.add(roomView, "room");

        // Các module chức năng đã xây dựng
        contentPanel.add(new KhachHangView(), "khachhang");
        contentPanel.add(new NhanVienView(), "quanlynhanvien");
        contentPanel.add(new HoaDonView(), "quanlyhoadon");
        contentPanel.add(new DichVuView(), "quanlydichvu");
        contentPanel.add(new MainBaoCaoView(), "baocao");

        // Layout chính
        JPanel main = new JPanel(new BorderLayout());
        main.add(sidebar, BorderLayout.WEST);
        main.add(contentPanel, BorderLayout.CENTER);

        add(main);

        // Menu bar
        createMenuBar();
    }

    private JPanel createPlaceholder(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 247, 250));
        JLabel lbl = new JLabel(title + " - Đang phát triển", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(new Color(120, 120, 120));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuHeThong = new JMenu("Hệ thống");
        JMenuItem itemDangXuat = new JMenuItem("Đăng xuất");
        JMenuItem itemThoat = new JMenuItem("Thoát");

        itemDangXuat.addActionListener(e -> dangXuat());
        itemThoat.addActionListener(e -> thoatChuongTrinh());

        menuHeThong.add(itemDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(itemThoat);
        menuBar.add(menuHeThong);
        setJMenuBar(menuBar);
    }

    private void dangXuat() {
        if (JOptionPane.showConfirmDialog(this, "Đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private void thoatChuongTrinh() {
        if (JOptionPane.showConfirmDialog(this, "Thoát chương trình?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}