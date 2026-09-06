package com.hotelmanagement.view.dichvu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import com.hotelmanagement.controller.DichVuController;
import com.hotelmanagement.model.dto.DichVuDTO;
import com.hotelmanagement.view.AppIcon;

/**
 * Lớp DichVuDialog - Dialog hiển thị form nhập/sửa thông tin dịch vụ
 * 
 * Chức năng:
 * - Cho phép nhập thông tin dịch vụ mới hoặc sửa thông tin dịch vụ hiện có
 * - Validate dữ liệu nhập (không để trống, giá > 0)
 * - Lưu dữ liệu vào cơ sở dữ liệu thông qua DichVuController
 * - Hiển thị thông báo thành công/thất bại
 * 
 * Thiết kế:
 * - Giao diện dạng Dialog với header, form và footer
 * - Sử dụng GridBagLayout để sắp xếp các thành phần trong form
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class DichVuDialog extends JDialog {

    private JTextField txtMaDV, txtTenDV;
    private JFormattedTextField txtGiaDV;
    private JButton btnSave, btnCancel;
    private DichVuDTO dto;
    private DichVuController controller = new DichVuController();
    private boolean isSuccess = false;

    // Luxury Look Colors
    private final Color PRIMARY_COLOR = new Color(30, 41, 59); // Slate-800
    private final Color GOLD_ACCENT = new Color(212, 175, 55); // Gold
    private final Color BG_COLOR = new Color(250, 250, 252);
    private final Color INPUT_BORDER = new Color(203, 213, 225); // Slate-300
    private final Color GREEN_BTN = new Color(16, 185, 129); // Emerald-500
    private final Color CANCEL_BTN = new Color(100, 116, 139); // Slate-500

    public DichVuDialog(Frame owner, String title, DichVuDTO dto) {
        super(owner, title, true);
        this.dto = dto;

        setSize(480, 480);
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
        lblTitle.setIcon(AppIcon.of(AppIcon.Type.HEADER, GOLD_ACCENT, 24));
        lblTitle.setIconTextGap(15);

        JLabel lblSub = new JLabel("  Cập nhật/Thêm mới dịch vụ khách sạn");
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
        txtMaDV = createStyledTextField(fieldFont);
        txtTenDV = createStyledTextField(fieldFont);

        // Formatter for GiaDV
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Long.class);
        formatter.setMinimum(0L);
        formatter.setMaximum(Long.MAX_VALUE);
        formatter.setAllowsInvalid(false);

        txtGiaDV = new JFormattedTextField(formatter);
        styleFormattedField(txtGiaDV, fieldFont);

        // Bind data if editing
        if (dto != null) {
            txtMaDV.setText(dto.getMaDV());
            txtMaDV.setEditable(false); // Không cho sửa Mã DV
            txtTenDV.setText(dto.getTenDichVu());
            if (dto.getGiaDV() != null) {
                txtGiaDV.setValue(dto.getGiaDV().longValue());
            }
        }

        // Add to grid
        int y = 0;
        addModernField(mainPanel, gbc, "MÃ DỊCH VỤ", txtMaDV, y++, labelFont);
        addModernField(mainPanel, gbc, "TÊN DỊCH VỤ", txtTenDV, y++, labelFont);
        addModernField(mainPanel, gbc, "GIÁ DỊCH VỤ (VNĐ)", txtGiaDV, y++, labelFont);

        // --- Buttons Panel ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(5, 15, 5, 15)));

        btnSave = createLuxuryButton("LƯU KHỞI TẠO", AppIcon.of(AppIcon.Type.CHECK, Color.WHITE, 16), GREEN_BTN,
                Color.WHITE);
        if (dto != null) {
            btnSave.setText("CẬP NHẬT");
        }
        btnCancel = createLuxuryButton("HỦY BỎ", AppIcon.of(AppIcon.Type.CROSS, Color.WHITE, 16), CANCEL_BTN,
                Color.WHITE);

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

    private void styleFormattedField(JFormattedTextField field, Font font) {
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
        gbc.insets = new Insets(15, 5, 4, 5);
        JLabel lbl = new JLabel();
        lbl.setFont(font);
        lbl.setForeground(PRIMARY_COLOR);
        panel.add(lbl, gbc);

        gbc.gridy = y * 2 + 1;
        gbc.insets = new Insets(0, 5, 5, 5);
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
            DichVuDTO current = (dto != null) ? dto : new DichVuDTO();
            current.setMaDV(txtMaDV.getText());
            current.setTenDichVu(txtTenDV.getText());
            if (txtGiaDV.getValue() != null) {
                current.setGiaDV(BigDecimal.valueOf(((Number) txtGiaDV.getValue()).longValue()));
            } else {
                current.setGiaDV(BigDecimal.ZERO);
            }

            if (dto == null) {
                controller.taoDichVu(current);
            } else {
                controller.capNhatDichVu(current);
            }

            isSuccess = true;
            JOptionPane.showMessageDialog(this, "Lưu dịch vụ thành công!", "LUXURY HOTEL",
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
