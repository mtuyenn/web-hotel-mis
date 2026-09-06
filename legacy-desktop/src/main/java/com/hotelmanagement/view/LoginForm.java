package com.hotelmanagement.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.hotelmanagement.model.dto.NhanVienDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.hotelmanagement.controller.LoginController;

/**
 * Lớp LoginForm - Giao diện đăng nhập hệ thống
 * 
 * Chức năng:
 * - Nhập tên đăng nhập và mật khẩu
 * - Hiển thị mật khẩu dưới dạng dấu chấm (có thể bật/tắt hiển thị)
 * - Xác thực thông tin đăng nhập với LoginController
 * - Mở MainFrame sau khi đăng nhập thành công
 * - Hiển thị thông báo lỗi nếu đăng nhập thất bại
 * 
 * Thiết kế:
 * - Giao diện chia 2 cột: hình nền (trái) và form đăng nhập (phải)
 * - Sử dụng FlatLaf cho giao diện hiện đại
 * - Có hiệu ứng hover cho nút đăng nhập
 * - Sử dụng AppIcon cho các icon vector
 * - Có placeholder text trong ô nhập liệu
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private boolean isPasswordVisible = false;
    private final LoginController loginController = new LoginController();

    public LoginForm() {
        setTitle("Đăng Nhập Hệ Thống - Luxury Hotel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.add(buildLeftPanel());
        mainContainer.add(buildRightPanel());

        add(mainContainer);
    }

    // ================= LEFT PANEL (Hình nền) =================
    private JPanel buildLeftPanel() {
        ImagePanel leftPanel = new ImagePanel("/images/hotel_bp.jpg");
        leftPanel.setLayout(new GridBagLayout());
        return leftPanel;
    }

    // ================= RIGHT PANEL =================
    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty(FlatClientProperties.STYLE, "arc:30;");
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblTitle.setForeground(new Color(15, 23, 42));
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(lblTitle, gbc);

        card.add(createLabel("Tên nhân viên", "USER"), gbc);

        gbc.insets = new Insets(5, 0, 10, 0);
        txtUsername = createTextField("Nhập tên đăng nhập (Tên nhân viên)...");
        card.add(txtUsername, gbc);

        card.add(createLabel("Mật khẩu", "LOCK"), gbc);

        gbc.insets = new Insets(5, 0, 15, 0);
        card.add(createPasswordPanel(), gbc);

        // Nút đăng nhập
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setPreferredSize(new Dimension(0, 50));
        btnLogin.setFont(new Font("SansSerif UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#1d4ed8; hoverBackground:#1e40af;");

        gbc.insets = new Insets(25, 0, 10, 0);
        card.add(btnLogin, gbc);

        // ==================== XỬ LÝ ĐĂNG NHẬP ====================
        btnLogin.addActionListener(e -> performLogin());

        // Nhấn Enter để đăng nhập
        txtUsername.addActionListener(e -> btnLogin.doClick());
        txtPassword.addActionListener(e -> btnLogin.doClick());

        rightPanel.add(card);
        return rightPanel;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.STYLE, "arc:12; focusWidth:2;");
        tf.setPreferredSize(new Dimension(320, 45));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(320, 45));
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "arc:12; focusWidth:2;");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 0, 35));
        wrapper.add(txtPassword, BorderLayout.CENTER);

        JLabel lblEye = new JLabel(AppIcon.of(AppIcon.Type.EYE_CLOSE, new Color(120, 120, 120), 18));
        lblEye.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isPasswordVisible = !isPasswordVisible;
                txtPassword.setEchoChar(isPasswordVisible ? (char) 0 : '•');
                lblEye.setIcon(AppIcon.of(
                        isPasswordVisible ? AppIcon.Type.EYE_OPEN : AppIcon.Type.EYE_CLOSE,
                        isPasswordVisible ? new Color(37, 99, 235) : new Color(120, 120, 120),
                        18));
            }
        });

        panel.add(wrapper, BorderLayout.CENTER);
        panel.add(lblEye, BorderLayout.EAST);
        return panel;
    }

    private JLabel createLabel(String text, String iconType) {
        JLabel lbl = new JLabel(text);
        lbl.setIcon(AppIcon.of(AppIcon.Type.valueOf(iconType), new Color(100, 116, 139), 18));
        lbl.setIconTextGap(10);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    // ==================== LOGIC ĐĂNG NHẬP ====================
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            NhanVienDTO user = loginController.login(username, password);

            // Đóng form login
            this.dispose();

            // Mở MainFrame với thông tin user real
            SwingUtilities.invokeLater(() -> {
                new MainFrame(user.getTenNV(), user.getChucVu()).setVisible(true);
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập hoặc mật khẩu không đúng!\nChi tiết: " + ex.getMessage(),
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            txtPassword.selectAll();
        }
    }

    // ImagePanel giữ nguyên
    static class ImagePanel extends JPanel {
        private final java.awt.image.BufferedImage img;

        public ImagePanel(String path) {
            java.awt.image.BufferedImage temp = null;
            try {
                java.net.URL url = getClass().getResource(path);
                if (url != null)
                    temp = javax.imageio.ImageIO.read(url);
            } catch (Exception ignored) {
            }
            this.img = temp;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(15, 23, 42));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

}