package com.hotelmanagement.view.khachhang;

import com.hotelmanagement.controller.KhachHangController;
import com.hotelmanagement.model.dto.KhachHangDTO;
import com.hotelmanagement.view.AppIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Lớp KhachHangDialog - Dialog hiển thị form nhập/sửa thông tin khách hàng
 * 
 * Chức năng:
 * - Cho phép nhập thông tin khách hàng mới hoặc sửa thông tin khách hàng hiện
 * có
 * - Validate dữ liệu nhập (không để trống, số điện thoại đúng định dạng, email
 * hợp lệ)
 * - Lưu dữ liệu vào cơ sở dữ liệu thông qua KhachHangController
 * - Hiển thị thông báo thành công/thất bại
 * 
 * Thiết kế:
 * - Giao diện dạng Dialog với header, form và footer
 * - Sử dụng GridBagLayout để sắp xếp các thành phần trong form
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class KhachHangDialog extends JDialog {

    private JTextField txtCCCD, txtTenKH, txtSDT, txtEmail, txtDiaChi;
    private JButton btnSave, btnCancel;
    private KhachHangDTO dto;
    private KhachHangController controller = new KhachHangController();
    private boolean isSuccess = false;

    // Luxury Look Colors
    private final Color PRIMARY_COLOR = new Color(30, 41, 59); // Slate-800
    private final Color GOLD_ACCENT = new Color(212, 175, 55); // Gold
    private final Color BG_COLOR = new Color(250, 250, 252);
    private final Color INPUT_BORDER = new Color(203, 213, 225); // Slate-300
    private final Color GREEN_BTN = new Color(16, 185, 129); // Emerald-500
    private final Color CANCEL_BTN = new Color(100, 116, 139); // Slate-500

    public KhachHangDialog(Frame owner, String title, KhachHangDTO dto) {
        super(owner, title, true);
        this.dto = dto;
        // Thiết lập màu cho Header Icon
        AppIcon.setGlobalColors(GOLD_ACCENT, PRIMARY_COLOR);

        setSize(480, 680); // Chiều dọc rộng rãi hơn
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

        JLabel lblSub = new JLabel("  Vui lòng điền đầy đủ các thông tin cần thiết");
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
        txtCCCD = createStyledTextField(fieldFont);
        txtTenKH = createStyledTextField(fieldFont);
        txtSDT = createStyledTextField(fieldFont);
        txtEmail = createStyledTextField(fieldFont);
        txtDiaChi = createStyledTextField(fieldFont);

        // Bind data if editing
        if (dto != null) {
            txtCCCD.setText(dto.getCccd());
            txtCCCD.setEditable(false);
            txtTenKH.setText(dto.getTenKH());
            txtSDT.setText(dto.getSoDienThoai());
            txtEmail.setText(dto.getEmail());
            txtDiaChi.setText(dto.getDiaChi());
        }

        // Add to grid (Modern vertical layout)
        int y = 0;
        addModernField(mainPanel, gbc, "CĂN CƯỚC CÔNG DÂN (CCCD)", txtCCCD, y++, labelFont);
        addModernField(mainPanel, gbc, "HỌ VÀ TÊN KHÁCH HÀNG", txtTenKH, y++, labelFont);
        addModernField(mainPanel, gbc, "SỐ ĐIỆN THOẠI LIÊN HỆ", txtSDT, y++, labelFont);
        addModernField(mainPanel, gbc, "ĐỊA CHỈ EMAIL", txtEmail, y++, labelFont);
        addModernField(mainPanel, gbc, "ĐỊA CHỈ THƯỜNG TRÚ", txtDiaChi, y++, labelFont);

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
        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField(Font font) {
        JTextField field = new JTextField(20);
        field.setFont(font);
        field.setForeground(new Color(15, 23, 42)); // Slate-900
        field.setPreferredSize(new Dimension(300, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                new EmptyBorder(5, 12, 5, 12)));

        // Focus highlighter
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
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }

            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }
        });

        return btn;
    }

    private void addModernField(JPanel panel, GridBagConstraints gbc, String labelTitle, JTextField field, int y,
            Font font) {
        gbc.gridx = 0;
        gbc.gridy = y * 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 5, 4, 5); // Spacing between fields
        JLabel lbl = new JLabel();
        lbl.setFont(font);
        lbl.setForeground(PRIMARY_COLOR);
        panel.add(lbl, gbc);

        gbc.gridy = y * 2 + 1;
        gbc.insets = new Insets(0, 5, 5, 5); // Closer to its label
        panel.add(field, gbc);

        Runnable updateLabel = () -> {
            if (field.getText() == null || field.getText().trim().isEmpty()) {
                lbl.setText("<html>" + labelTitle + " <font color='red'>*</font></html>");
            } else {
                lbl.setText(labelTitle);
            }
        };
        updateLabel.run();

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateLabel.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateLabel.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateLabel.run();
            }
        });
    }

    private void save() {
        try {
            KhachHangDTO current = (dto != null) ? dto : new KhachHangDTO();
            current.setCccd(txtCCCD.getText());
            current.setTenKH(txtTenKH.getText());
            current.setSoDienThoai(txtSDT.getText());
            current.setEmail(txtEmail.getText());
            current.setDiaChi(txtDiaChi.getText());

            if (dto == null) {
                controller.taoKhachHang(current);
            } else {
                controller.capNhatKhachHang(current);
            }

            isSuccess = true;
            JOptionPane.showMessageDialog(this, "Lưu thông tin thành công!", "LUXURY HOTEL",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}