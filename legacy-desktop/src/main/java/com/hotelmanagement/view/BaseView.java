package com.hotelmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Lớp BaseView - Nền tảng chung cho tất cả các View trong dự án
 * Giúp giảm lặp code và giữ giao diện đồng nhất
 */
public abstract class BaseView extends JPanel {

    protected JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;
    protected JTextField txtSearch;
    protected String searchPlaceholder = "Nhập CCCD để tìm khách hàng";
    protected JTable table;
    protected DefaultTableModel model;
    protected JPanel contentPanel;

    public BaseView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 247, 250));

        initTopPanel();
        initContentPanel();
    }

    /**
     * Khởi tạo thanh công cụ phía trên (các nút chức năng)
     */
    private void initTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)));

        // Các nút chức năng
        btnAdd = createStyledButton("Thêm mới", AppIcon.Type.ADD, new Color(52, 152, 219));
        btnEdit = createStyledButton("Sửa", AppIcon.Type.EDIT, new Color(243, 156, 18));
        btnDelete = createStyledButton("Xóa", AppIcon.Type.DELETE, new Color(231, 76, 60));
        btnRefresh = createStyledButton("Làm mới", AppIcon.Type.REFRESH, new Color(46, 204, 113));
        btnSearch = createStyledButton("Tìm kiếm", AppIcon.Type.SEARCH, new Color(52, 73, 94));

        txtSearch = new JTextField(25) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(150, 150, 150));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    int fontMetricsHeight = g2.getFontMetrics().getHeight();
                    int y = (getHeight() - fontMetricsHeight) / 2 + g2.getFontMetrics().getAscent();
                    g2.drawString(searchPlaceholder, 8, y);
                    g2.dispose();
                }
            }
        };
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearch.repaint();
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearch.repaint();
            }
        });

        // Hàng 1: Nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);

        // Hàng 2: Ô nhập & nút tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Tạo nút
     */
    private JButton createStyledButton(String text, AppIcon.Type iconType, Color bgColor) {
        Icon icon = AppIcon.of(iconType, Color.BLACK, 16); // icon màu đen kích thước 16

        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    /**
     * Khởi tạo khu vực nội dung chính 
     */
    private void initContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        add(contentPanel, BorderLayout.CENTER);
    }

    // ==================== Các phương thức abstract ====================

    /**
     * Load dữ liệu vào bảng hoặc grid
     */
    protected abstract void loadData();

    /**
     * Mở form thêm mới
     */
    protected abstract void addNew();

    /**
     * Mở form sửa bản ghi đang chọn
     */
    protected abstract void editSelected();

    /**
     * Xóa bản ghi đang chọn
     */
    protected abstract void deleteSelected();

    /**
     * Thực hiện tìm kiếm
     */
    protected abstract void search();

    // ==================== Getter tiện ích ====================

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    /**
     * Hiển thị thông báo thành công
     */
    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị thông báo lỗi
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

}