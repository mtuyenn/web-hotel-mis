package com.hotelmanagement.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp SidebarMenu - Thanh điều hướng bên trái của ứng dụng
 * 
 * Chức năng:
 * - Hiển thị danh sách các chức năng chính của hệ thống
 * - Cho phép chuyển đổi giữa các màn hình khác nhau
 * - Hiển thị thông tin người dùng đang đăng nhập
 * - Có hiệu ứng hover và active cho các nút menu
 * 
 * Thiết kế:
 * - Giao diện chia thành 3 phần: Logo (trên), Menu (giữa), Footer (dưới)
 * - Sử dụng CardLayout để quản lý các màn hình
 * - Sử dụng GridBagLayout để sắp xếp các nút menu
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Sử dụng AppIcon cho các icon vector
 */
public class SidebarMenu extends JPanel {

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final Map<String, JButton> menuButtons = new HashMap<>();
    private JButton activeButton = null;

    // Màu chủ đạo
    private final Color SIDEBAR_BG = new Color(34, 47, 62);
    private final Color HOVER_BG = new Color(52, 73, 94);
    private final Color ACTIVE_BG = new Color(52, 152, 219);

    public SidebarMenu(CardLayout cardLayout, JPanel contentPanel, String tenNhanVien) {
        this.cardLayout = cardLayout;
        this.contentPanel = contentPanel;

        setLayout(new BorderLayout());
        setBackground(SIDEBAR_BG);
        setPreferredSize(new Dimension(250, 0));

        // ==================== LOGO ====================
        JPanel logoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 25, 0));

        JLabel lblLogo = new JLabel("LUXURY HOTEL", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Times New Roman", Font.BOLD, 26));
        lblLogo.setForeground(new Color(212, 175, 55));

        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        starPanel.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            starPanel.add(new JLabel(AppIcon.of(AppIcon.Type.STAR, new Color(212, 175, 55), 20)));
        }

        logoPanel.add(lblLogo);
        logoPanel.add(starPanel);
        add(logoPanel, BorderLayout.NORTH);

        // ==================== MENU ITEMS ====================
        String[] menuItems = {
                "Quản lý phòng",
                "Khách hàng",
                "Quản lý hóa đơn",
                "Quản lý dịch vụ",
                "Báo cáo",
                "Nhân viên"
        };

        Color[] iconColors = {
                new Color(52, 152, 219),
                new Color(46, 204, 113),
                new Color(231, 76, 60),
                new Color(155, 89, 182),
                new Color(241, 196, 15),
                new Color(230, 126, 34)
        };

        JPanel menuContainer = new JPanel(new GridBagLayout());
        menuContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ép các component bung ngang 100%
        gbc.insets = new Insets(0, 0, 6, 0); // Khoảng cách nhỏ giữa các nút

        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i], iconColors[i]);
            menuContainer.add(btn, gbc);
            menuButtons.put(menuItems[i], btn);
        }

        JPanel wrapperMenu = new JPanel(new BorderLayout());
        wrapperMenu.setOpaque(false);
        wrapperMenu.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapperMenu.add(menuContainer, BorderLayout.NORTH); // Đẩy menu lên trên cùng

        add(wrapperMenu, BorderLayout.CENTER);

        // ==================== FOOTER ====================
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));

        JLabel lblUser = new JLabel("Nhân viên: " + (tenNhanVien != null ? tenNhanVien : "Admin"));
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(200, 200, 200));
        footer.add(lblUser);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text, Color iconColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(new Color(230, 230, 230));
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        // Quan trọng: Chỉnh các nút ra sát lề trái và chiếm toàn bộ chiều rộng
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 15)); // Padding lề trái đẹp hơn

        // Icon tròn + chữ cái
        Icon customIcon = AppIcon.menu(text.substring(0, 1), iconColor, 22);
        btn.setIcon(customIcon);
        btn.setIconTextGap(15); // Tăng gap một chút vì AppIcon size nhỏ hơn icon cũ (32)

        // Hover & Active effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeButton != null) {
                activeButton.setBackground(SIDEBAR_BG);
            }
            activeButton = btn;
            btn.setBackground(ACTIVE_BG);

            // Mapping chuẩn xác tuyệt đối các tên view tương ứng với bên MainFrame.java
            String cardName = "";
            switch (text) {
                case "Quản lý phòng":
                    cardName = "room";
                    break;
                case "Khách hàng":
                    cardName = "khachhang";
                    break;
                case "Nhân viên":
                    cardName = "quanlynhanvien";
                    break;
                case "Quản lý hóa đơn":
                    cardName = "quanlyhoadon";
                    break;
                case "Quản lý dịch vụ":
                    cardName = "quanlydichvu";
                    break;
                case "Báo cáo":
                    cardName = "baocao";
                    break;
            }
            if (!cardName.isEmpty()) {
                cardLayout.show(contentPanel, cardName);
            }
        });

        return btn;
    }

}