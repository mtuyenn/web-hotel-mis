package com.hotelmanagement.view.baocao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Lớp KpiCardPanel - Panel hiển thị chỉ số KPI (Key Performance Indicator)
 * 
 * Chức năng:
 * - Hiển thị tiêu đề, giá trị và phần trăm thay đổi
 * - Có 3 chế độ hiển thị: Primary (chính), Info (thông tin), Neutral (trung
 * bình)
 * - Tự động vẽ background với góc bo tròn và màu sắc tương ứng
 * - Hỗ trợ cập nhật dữ liệu động
 * 
 * Thiết kế:
 * - Sử dụng BorderLayout để sắp xếp các thành phần
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh màu sắc text dựa trên chế độ hiển thị
 */
public class KpiCardPanel extends JPanel {

    private String title;
    private String value;
    private String percentChange;
    private boolean isPositive;

    public KpiCardPanel(String title, String value, String percentChange, boolean isPositive) {
        this.title = title;
        this.value = value;
        this.percentChange = percentChange;
        this.isPositive = isPositive;

        setOpaque(false); // Make background transparent so custom painting works
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setPreferredSize(new Dimension(200, 120));

        // Tittle label
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(100, 116, 139)); // Slate 500
        add(lblTitle, BorderLayout.NORTH);

        // Value label
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(new Color(15, 23, 42)); // Slate 900
        lblValue.setVerticalAlignment(SwingConstants.CENTER);
        add(lblValue, BorderLayout.CENTER);

        // Percent change label
        if (percentChange != null && !percentChange.isEmpty()) {
            JLabel lblPercent = new JLabel(percentChange);
            lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (isPositive) {
                lblPercent.setForeground(new Color(16, 185, 129)); // Emerald 500
                lblPercent.setText("▲ " + percentChange);
            } else {
                lblPercent.setForeground(new Color(239, 68, 68)); // Red 500
                lblPercent.setText("▼ " + percentChange);
            }
            add(lblPercent, BorderLayout.SOUTH);
        }
    }

    // --- Styling options for Visual Hierarchy ---
    public void setPrimaryStyle() {
        setBackground(new Color(15, 23, 42)); // Slate 900
        setOpaque(true);
        // We will override paintComponent so making it opaque=false and filling in
        // paintComponent is better
        setOpaque(false);
        putClientProperty("style", "PRIMARY");
        repaint();
    }

    public void setInfoStyle() {
        setOpaque(false);
        putClientProperty("style", "INFO");
        repaint();
    }

    public void setNeutralStyle() {
        setOpaque(false);
        putClientProperty("style", "NEUTRAL");
        repaint();
    }

    public void updateData(String title, String value, String percentChange, boolean isPositive) {
        this.title = title;
        updateData(value, percentChange, isPositive);
    }

    // Setter to update value dynamically
    public void updateData(String value, String percentChange, boolean isPositive) {
        this.value = value;
        this.percentChange = percentChange;
        this.isPositive = isPositive;
        removeAll();

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(100, 116, 139));
        add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(new Color(15, 23, 42));
        lblValue.setVerticalAlignment(SwingConstants.CENTER);
        add(lblValue, BorderLayout.CENTER);

        if (percentChange != null && !percentChange.isEmpty()) {
            JLabel lblPercent = new JLabel(percentChange);
            lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (isPositive) {
                lblPercent.setForeground(new Color(34, 197, 94));
                lblPercent.setText("▲ " + percentChange);
            } else {
                lblPercent.setForeground(new Color(239, 68, 68));
                lblPercent.setText("▼ " + percentChange);
            }
            add(lblPercent, BorderLayout.SOUTH);
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String style = (String) getClientProperty("style");
        if (style == null)
            style = "NEUTRAL";

        Color bg = Color.WHITE;
        Color border = new Color(226, 232, 240); // Slate 200
        Color titleCol = new Color(100, 116, 139);
        Color valCol = new Color(15, 23, 42);

        if ("PRIMARY".equals(style)) {
            bg = new Color(15, 23, 42); // Slate 900
            border = new Color(15, 23, 42);
            titleCol = new Color(148, 163, 184); // Slate 400
            valCol = Color.WHITE;
        } else if ("INFO".equals(style)) {
            bg = new Color(240, 249, 255); // Sky 50
            border = new Color(186, 230, 253); // Sky 200
            titleCol = new Color(3, 105, 161); // Sky 700
            valCol = new Color(12, 74, 110); // Sky 900
        }

        // Fill background with rounded corners
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Draw border
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Update labels color
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (lbl.getFont().getSize() == 13)
                    lbl.setForeground(titleCol);
                else if (lbl.getFont().getSize() == 24)
                    lbl.setForeground(valCol);
                // Percent label color keeps original red/green unless we are in primary dark?
                // Actually keep it, but maybe adjust for dark mode
            }
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
