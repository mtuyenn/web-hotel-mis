package com.hotelmanagement.view.baocao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Lớp SimpleChartPanel - Panel hiển thị biểu đồ tùy chỉnh
 * 
 * Chức năng:
 * - Hỗ trợ 4 loại biểu đồ: Bar, Line, Area, Donut
 * - Vẽ biểu đồ bằng Java 2D (Graphics2D)
 * - Tự động tính toán kích thước và vị trí các thành phần
 * - Hỗ trợ hiển thị tooltip khi di chuột qua các điểm dữ liệu
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 * 
 * Thiết kế:
 * - Sử dụng BorderLayout để sắp xếp các thành phần
 * - Sử dụng GridBagLayout để sắp xếp các biểu đồ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class SimpleChartPanel extends JPanel {

    public enum ChartType {
        BAR, LINE, AREA, DONUT
    }

    private ChartType chartType = ChartType.BAR;
    private Map<String, Double> data = new LinkedHashMap<>();
    private String title = "";
    private boolean yAxisPercentage = false;
    private final Color chartColor = new Color(52, 152, 219);
    private final Color GRID_COLOR = new Color(240, 240, 240);
    private final Color AXIS_COLOR = new Color(150, 150, 150);

    public SimpleChartPanel(String title, ChartType type) {
        this.title = title;
        this.chartType = type;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setToolTipText(""); // Enable hover tooltips natively
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public void setYAxisPercentage(boolean yAxisPercentage) {
        this.yAxisPercentage = yAxisPercentage;
        repaint();
    }

    public void setChartType(ChartType type) {
        this.chartType = type;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.drawString("Không có dữ liệu hiển thị", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 60;
        int labelPadding = 40;
        int width = getWidth();
        int height = getHeight();

        // Tìm giá trị lớn nhất trục Y
        double maxVal = 0;
        double sumVal = 0; // for donut chart
        for (Double val : data.values()) {
            if (val > maxVal)
                maxVal = val;
            sumVal += val;
        }
        maxVal = maxVal * 1.2;
        if (maxVal == 0)
            maxVal = 100;

        List<String> keys = new ArrayList<>(data.keySet());
        int n = keys.size();

        if (chartType == ChartType.DONUT) {
            drawDonutChart(g2, width, height, keys, sumVal);
            g2.dispose();
            return;
        }

        // Vẽ Grid và Labels trục Y
        int numYMarkers = 5;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        String lastLabel = "";
        for (int i = 0; i <= numYMarkers; i++) {
            int y0 = height - padding - labelPadding - (i * (height - 2 * padding - labelPadding) / numYMarkers);

            double yVal = maxVal * i / numYMarkers;
            String label;
            if (yVal > 0 && yVal < 1) {
                label = String.format("%,.3f", yVal);
            } else if (yVal >= 1 && yVal < 10) {
                label = String.format("%,.1f", yVal);
            } else {
                label = String.format("%,.0f", yVal);
            }

            if (yAxisPercentage) {
                label += "%";
            }

            if (!label.equals(lastLabel) || i == 0) {
                g2.setColor(GRID_COLOR);
                g2.drawLine(padding + labelPadding, y0, width - padding, y0);

                g2.setColor(AXIS_COLOR);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, padding + labelPadding - fm.stringWidth(label) - 10, y0 + (fm.getAscent() / 2));
                lastLabel = label;
            }
        }

        // Vẽ trục chính
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(padding + labelPadding, height - padding - labelPadding, padding + labelPadding, padding); // Y
        g2.drawLine(padding + labelPadding, height - padding - labelPadding, width - padding,
                height - padding - labelPadding); // X

        double barWidth = (double) (width - 2 * padding - labelPadding) / n;

        if (chartType == ChartType.BAR) {
            for (int i = 0; i < n; i++) {
                double val = data.get(keys.get(i));
                int barHeight = (int) ((val / maxVal) * (height - 2 * padding - labelPadding));
                int x = (int) (padding + labelPadding + i * barWidth + barWidth * 0.2);
                int y = height - padding - labelPadding - barHeight;
                int w = (int) (barWidth * 0.6);

                GradientPaint gp = new GradientPaint(x, y, chartColor, x, y + barHeight, chartColor.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, w, barHeight, 5, 5);

                drawXLabel(g2, keys.get(i), (int) (padding + labelPadding + i * barWidth + barWidth / 2),
                        height - padding);
            }
        } else {
            // LINE or AREA chart
            Path2D path = new Path2D.Double();
            Path2D areaPath = new Path2D.Double(); // For area fill

            g2.setColor(chartColor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (int i = 0; i < n; i++) {
                double val = data.get(keys.get(i));
                int px = (int) (padding + labelPadding + i * barWidth + barWidth / 2);
                int py = (int) (height - padding - labelPadding
                        - (val / maxVal) * (height - 2 * padding - labelPadding));

                if (i == 0) {
                    path.moveTo(px, py);
                    areaPath.moveTo(px, height - padding - labelPadding); // Start from bottom
                    areaPath.lineTo(px, py);
                } else {
                    path.lineTo(px, py);
                    areaPath.lineTo(px, py);
                }

                if (i == n - 1) {
                    areaPath.lineTo(px, height - padding - labelPadding); // Close to bottom
                }

                drawXLabel(g2, keys.get(i), px, height - padding);
            }

            if (chartType == ChartType.AREA) {
                GradientPaint areaGradient = new GradientPaint(
                        0, padding, new Color(chartColor.getRed(), chartColor.getGreen(), chartColor.getBlue(), 80),
                        0, height - padding - labelPadding,
                        new Color(chartColor.getRed(), chartColor.getGreen(), chartColor.getBlue(), 10));
                g2.setPaint(areaGradient);
                g2.fill(areaPath);
            }

            g2.setColor(chartColor);
            g2.draw(path);

            // Vẽ nút điểm
            for (int i = 0; i < n; i++) {
                double val = data.get(keys.get(i));
                int px = (int) (padding + labelPadding + i * barWidth + barWidth / 2);
                int py = (int) (height - padding - labelPadding
                        - (val / maxVal) * (height - 2 * padding - labelPadding));
                g2.setColor(Color.WHITE);
                g2.fillOval(px - 4, py - 4, 8, 8);
                g2.setColor(chartColor);
                g2.drawOval(px - 4, py - 4, 8, 8);
            }
        }

        // Vẽ Title
        g2.setColor(new Color(45, 52, 54));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(title, (width - tfm.stringWidth(title)) / 2, 30);

        g2.dispose();
    }

    private void drawDonutChart(Graphics2D g2, int width, int height, List<String> keys, double sumVal) {
        int chartSize = Math.min(width, height) - 150;
        int totalContentWidth = chartSize + 220; // 50px khoảng cách + ~170px độ rộng text
        int cx = width / 2 - totalContentWidth / 2;
        if (cx < 10)
            cx = 10;
        int cy = height / 2 - chartSize / 2 + 10;

        double currentAngle = 0;
        int legendX = cx + chartSize + 25; // Dời qua trái 25px
        int legendY = cy + 20;

        Color[] colors = {
                new Color(34, 197, 94), // Emerald 500 (Đang sử dụng)
                new Color(148, 163, 184), // Slate 400 (Trống)
                new Color(249, 115, 22), // Orange 500 (Bảo trì/Dọn)
                new Color(59, 130, 246) // Blue 500
        };

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        for (int i = 0; i < keys.size(); i++) {
            double val = data.get(keys.get(i));
            if (val == 0)
                continue;

            double angle = (val / sumVal) * 360.0;
            g2.setColor(colors[i % colors.length]);
            g2.fillArc(cx, cy, chartSize, chartSize, (int) currentAngle, (int) Math.ceil(angle));

            // Draw percentage on the slice
            double midAngle = currentAngle + angle / 2.0;
            double percentage = (val / sumVal) * 100.0;

            // Center of the donut arc
            int textR = (int) (chartSize * 0.4);
            int textX = cx + chartSize / 2 + (int) (Math.cos(Math.toRadians(midAngle)) * textR);
            int textY = cy + chartSize / 2 - (int) (Math.sin(Math.toRadians(midAngle)) * textR); // Y axis inverted in
                                                                                                 // Java swing

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String slicePctText = String.format("%.1f%%", percentage);
            FontMetrics sliceFm = g2.getFontMetrics();
            g2.drawString(slicePctText, textX - sliceFm.stringWidth(slicePctText) / 2, textY + 5);

            // Draw Legend
            int currentLegendY = legendY + i * 30; // thụt xuống 30px mỗi dòng
            g2.setColor(colors[i % colors.length]); // Lặp lại đúng màu
            g2.fillRect(legendX, currentLegendY, 12, 12);

            g2.setColor(new Color(100, 116, 139)); // Slate 500 (Màu chữ)
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Đảm bảo đúng font cho chữ
            String legendText = keys.get(i); // Chỉ hiển thị tên trạng thái
            g2.drawString(legendText, legendX + 20, currentLegendY + 11);

            currentAngle += angle;
        }

        // Draw inner circle for Donut effect
        int innerSize = (int) (chartSize * 0.6);
        int icx = cx + (chartSize - innerSize) / 2;
        int icy = cy + (chartSize - innerSize) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(icx, icy, innerSize, innerSize);

        // Draw total in center
        g2.setColor(new Color(15, 23, 42)); // Slate 900
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        String totalText = String.format("Tổng: %.0f", sumVal);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalText, cx + chartSize / 2 - fm.stringWidth(totalText) / 2, cy + chartSize / 2 + 5);

        // Draw title
        g2.setColor(new Color(45, 52, 54));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(title, (width - tfm.stringWidth(title)) / 2, 30);
    }

    private void drawXLabel(Graphics2D g2, String label, int centerX, int y) {
        g2.setColor(AXIS_COLOR);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        if (label.length() > 5 && getWidth() < 600) {
            // Nghiêng nhãn nếu quá chật
            g2.translate(centerX, y);
            g2.rotate(Math.toRadians(45));
            g2.drawString(label, 0, 0);
            g2.rotate(-Math.toRadians(45));
            g2.translate(-centerX, -y);
        } else {
            g2.drawString(label, centerX - fm.stringWidth(label) / 2, y + fm.getAscent() + 5);
        }
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        if (data == null || data.isEmpty())
            return null;
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (chartType == ChartType.DONUT) {
            int width = getWidth();
            int height = getHeight();
            int chartSize = Math.min(width, height) - 80;
            int totalContentWidth = chartSize + 140;
            int cx = width / 2 - totalContentWidth / 2;
            if (cx < 10)
                cx = 10;
            int cy = height / 2 - chartSize / 2 + 10;

            double dx = mouseX - (cx + chartSize / 2.0);
            double dy = mouseY - (cy + chartSize / 2.0);
            double r = Math.sqrt(dx * dx + dy * dy);

            if (r > chartSize / 2.0 * 0.6 && r <= chartSize / 2.0) { // inside the donut ring
                // Java Swing fillArc considers 0 at 3 o'clock, increasing counter-clockwise. Y
                // is inverted.
                double angleStr = Math.toDegrees(Math.atan2(-dy, dx));
                if (angleStr < 0)
                    angleStr += 360;

                double current = 0;
                double sumVal = data.values().stream().mapToDouble(Double::doubleValue).sum();
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    double a = (entry.getValue() / sumVal) * 360.0;
                    if (angleStr >= current && angleStr < current + a) {
                        return String.format("%s: %.0f (%.1f%%)", entry.getKey(), entry.getValue(),
                                (entry.getValue() / sumVal) * 100);
                    }
                    current += a;
                }
            }
        } else {
            // Hover logic for Bar/Line chart
            int padding = 60;
            int labelPadding = 40;
            int width = getWidth();
            List<String> keys = new ArrayList<>(data.keySet());
            int n = keys.size();
            if (n == 0)
                return null;
            double barWidth = (double) (width - 2 * padding - labelPadding) / n;

            int index = (int) ((mouseX - (padding + labelPadding)) / barWidth);
            if (index >= 0 && index < n) {
                double val = data.get(keys.get(index));
                String append = yAxisPercentage ? "%" : "";
                if (val > 0 && val < 1) {
                    return String.format("%s: %,.3f%s", keys.get(index), val, append);
                } else if (val >= 1 && val < 10) {
                    return String.format("%s: %,.1f%s", keys.get(index), val, append);
                } else {
                    return String.format("%s: %,.0f%s", keys.get(index), val, append);
                }
            }
        }
        return super.getToolTipText(e);
    }
}