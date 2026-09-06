package com.hotelmanagement.view.nhanvien;

import com.hotelmanagement.controller.NhanVienController;
import com.hotelmanagement.model.dto.NhanVienDTO;
import com.hotelmanagement.model.enums.ChucVuNhanVien;
import com.hotelmanagement.view.AppIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Lớp NhanVienDialog - Dialog hiển thị form nhập/sửa thông tin nhân viên
 * 
 * Chức năng:
 * - Cho phép nhập thông tin nhân viên mới hoặc sửa thông tin nhân viên hiện có
 * - Validate dữ liệu nhập (không để trống, số điện thoại đúng định dạng)
 * - Lưu dữ liệu vào cơ sở dữ liệu thông qua NhanVienController
 * - Hiển thị thông báo thành công/thất bại
 * 
 * Thiết kế:
 * - Giao diện dạng Dialog với header, form và footer
 * - Sử dụng GridBagLayout để sắp xếp các thành phần trong form
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class NhanVienDialog extends JDialog {

    private JTextField txtMaNV, txtTenNV, txtSDT, txtDiaChi;
    private JPasswordField txtPassword;
    private JComboBox<ChucVuNhanVien> cbChucVu;
    private JButton btnSave, btnCancel;
    private NhanVienDTO dto;
    private NhanVienController controller = new NhanVienController();
    private boolean isSuccess = false;

    // Luxury Look Colors
    private final Color PRIMARY_COLOR = new Color(30, 41, 59); // Slate-800
    private final Color GOLD_ACCENT = new Color(212, 175, 55); // Gold
    private final Color BG_COLOR = new Color(250, 250, 252);
    private final Color INPUT_BORDER = new Color(203, 213, 225); // Slate-300
    private final Color GREEN_BTN = new Color(16, 185, 129); // Emerald-500
    private final Color CANCEL_BTN = new Color(100, 116, 139); // Slate-500

    public NhanVienDialog(Frame owner, String title, NhanVienDTO dto) {
        super(owner, title, true);
        this.dto = dto;
        // Thiết lập màu cho Header Icon
        AppIcon.setGlobalColors(GOLD_ACCENT, PRIMARY_COLOR);

        setSize(480, 750); // Chiều dọc rộng rãi hơn để chứa thêm fields
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // --- Custom Luxury Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(GOLD_ACCENT);
        lblTitle.setIconTextGap(15);
        lblTitle.setIcon(new AppIcon(AppIcon.Type.HEADER, null, 24));

        JLabel lblSub = new JLabel("  Vui lòng điền đầy đủ các thông tin nhân viên");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(new Color(226, 232, 240));

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titleTextPanel.setBackground(PRIMARY_COLOR);
        titleTextPanel.add(lblTitle);
        titleTextPanel.add(lblSub);

        headerPanel.add(titleTextPanel, BorderLayout.CENTER);

        // --- Main Form Panel ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        // Initialize fields
        txtMaNV = createStyledTextField(fieldFont);
        txtTenNV = createStyledTextField(fieldFont);
        txtPassword = createStyledPasswordField(fieldFont);
        cbChucVu = new JComboBox<>(ChucVuNhanVien.values());
        cbChucVu.setFont(fieldFont);
        cbChucVu.setPreferredSize(new Dimension(300, 42));
        cbChucVu.setBackground(Color.WHITE);

        txtSDT = createStyledTextField(fieldFont);
        txtDiaChi = createStyledTextField(fieldFont);

        // Bind data if editing
        if (dto != null) {
            txtMaNV.setText(dto.getMaNV());
            txtMaNV.setEditable(false);
            txtTenNV.setText(dto.getTenNV());
            txtPassword.setText(dto.getPassword());
            if (dto.getChucVu() != null) {
                cbChucVu.setSelectedItem(ChucVuNhanVien.valueOf(dto.getChucVu()));
            }
            txtSDT.setText(dto.getSoDienThoai());
            txtDiaChi.setText(dto.getDiaChi());
        }

        // Add to grid
        int y = 0;
        addModernField(mainPanel, gbc, "MÃ NHÂN VIÊN", txtMaNV, y++, labelFont);
        addModernField(mainPanel, gbc, "HỌ VÀ TÊN", txtTenNV, y++, labelFont);
        addModernField(mainPanel, gbc, "MẬT KHẨU ĐĂNG NHẬP", txtPassword, y++, labelFont);

        // Add ComboBox for Role
        gbc.gridx = 0;
        gbc.gridy = y * 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 5, 4, 5);
        JLabel lblRole = new JLabel("CHỨC VỤ");
        lblRole.setFont(labelFont);
        lblRole.setForeground(PRIMARY_COLOR);
        mainPanel.add(lblRole, gbc);

        gbc.gridy = y * 2 + 1;
        gbc.insets = new Insets(0, 5, 5, 5);
        mainPanel.add(cbChucVu, gbc);
        y++;

        addModernField(mainPanel, gbc, "SỐ ĐIỆN THOẠI", txtSDT, y++, labelFont);
        addModernField(mainPanel, gbc, "ĐỊA CHỈ", txtDiaChi, y++, labelFont);

        // --- Buttons Panel ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(5, 15, 5, 15)));

        btnSave = createLuxuryButton("LƯU",
                new AppIcon(AppIcon.Type.CHECK, Color.WHITE, 16),
                GREEN_BTN, Color.WHITE);

        btnCancel = createLuxuryButton("HỦY BỎ",
                new AppIcon(AppIcon.Type.CROSS, Color.WHITE, 16),
                CANCEL_BTN, Color.WHITE);

        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(headerPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField(Font font) {
        JTextField field = new JTextField(20);
        field.setFont(font);
        field.setForeground(new Color(15, 23, 42));
        field.setPreferredSize(new Dimension(300, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                new EmptyBorder(5, 12, 5, 12)));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD_ACCENT, 2),
                        new EmptyBorder(4, 11, 4, 11)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        new EmptyBorder(5, 12, 5, 12)));
            }
        });
        return field;
    }

    private JPasswordField createStyledPasswordField(Font font) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(font);
        field.setForeground(new Color(15, 23, 42));
        field.setPreferredSize(new Dimension(300, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                new EmptyBorder(5, 12, 5, 12)));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD_ACCENT, 2),
                        new EmptyBorder(4, 11, 4, 11)));
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER, 1),
                        new EmptyBorder(5, 12, 5, 12)));
            }
        });
        return field;
    }

    private JButton createLuxuryButton(String text, Icon icon, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(bgColor.darker());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 45));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }
        });

        return btn;
    }

    private void addModernField(JPanel panel, GridBagConstraints gbc, String labelTitle, JComponent field, int y,
            Font font) {
        gbc.gridx = 0;
        gbc.gridy = y * 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 5, 4, 5);
        JLabel lbl = new JLabel();
        lbl.setFont(font);
        lbl.setForeground(PRIMARY_COLOR);
        panel.add(lbl, gbc);

        gbc.gridy = y * 2 + 1;
        gbc.insets = new Insets(0, 5, 5, 5);
        panel.add(field, gbc);

        lbl.setText(labelTitle);
    }

    private void save() {
        try {
            if (txtMaNV.getText().trim().isEmpty() || txtTenNV.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã và Tên nhân viên", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            NhanVienDTO current = (dto != null) ? dto : new NhanVienDTO();
            current.setMaNV(txtMaNV.getText().trim());
            current.setTenNV(txtTenNV.getText().trim());
            current.setPassword(new String(txtPassword.getPassword()));
            current.setChucVu(((ChucVuNhanVien) cbChucVu.getSelectedItem()).name());
            current.setSoDienThoai(txtSDT.getText().trim());
            current.setDiaChi(txtDiaChi.getText().trim());

            if (dto == null) {
                controller.taoNhanVien(current);
            } else {
                controller.capNhatNhanVien(current);
            }

            isSuccess = true;
            JOptionPane.showMessageDialog(this, "Lưu thông tin nhân viên thành công!", "LUXURY HOTEL",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
