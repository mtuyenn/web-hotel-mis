package com.hotelmanagement.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Lớp AppIcon - Quản lý tất cả icon vector trong hệ thống
 * Giúp giao diện đồng nhất, dễ mở rộng và nhẹ
 */
public class AppIcon implements Icon {

    public enum Type {
        // Icons cho BaseView (CRUD + Search)
        ADD, EDIT, DELETE, REFRESH, SEARCH,

        // Icons cho Dialog
        HEADER, CHECK, CROSS,

        // Icons chức năng
        USER, MONEY, CALENDAR, CHECKIN, CHECKOUT, CARD, TAG,
        LOCK, EYE_OPEN, EYE_CLOSE, STAR, MENU_ITEM
    }

    private final Type type;
    private final Color color;
    private final int size;
    private final String symbol;

    public AppIcon(Type type, Color color, int size) {
        this(type, null, color, size);
    }

    public AppIcon(Type type, String symbol, Color color, int size) {
        this.type = type;
        this.symbol = symbol;
        this.color = color;
        this.size = size;
    }

    // ==================== FACTORY METHODS ====================

    public static AppIcon of(Type type, int size) {
        return new AppIcon(type, Color.BLACK, size);
    }

    public static AppIcon of(Type type, Color color, int size) {
        return new AppIcon(type, color, size);
    }

    public static AppIcon menu(String symbol, Color color, int size) {
        return new AppIcon(Type.MENU_ITEM, symbol, color, size);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Anti-alias mượt mà
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int s = size;

        switch (type) {
            case ADD -> paintAdd(g2, x, y, s);
            case EDIT -> paintEdit(g2, x, y, s);
            case DELETE -> paintDelete(g2, x, y, s);
            case REFRESH -> paintRefresh(g2, x, y, s);
            case SEARCH -> paintSearch(g2, x, y, s);

            case HEADER -> paintHeader(g2, x, y, s);
            case CHECK -> paintCheck(g2, x, y, s);
            case CROSS -> paintCross(g2, x, y, s);

            case USER -> paintUser(g2, x, y, s);
            case MONEY -> paintMoney(g2, x, y, s);
            case CARD -> paintCard(g2, x, y, s);
            case CALENDAR -> paintCalendar(g2, x, y, s);
            case CHECKIN -> paintCheckIn(g2, x, y, s);
            case CHECKOUT -> paintCheckOut(g2, x, y, s);
            case TAG -> paintTag(g2, x, y, s);

            case LOCK -> paintLock(g2, x, y, s);
            case EYE_OPEN -> paintEyeOpen(g2, x, y, s);
            case EYE_CLOSE -> paintEyeClose(g2, x, y, s);
            case STAR -> paintStar(g2, x, y, s);
            case MENU_ITEM -> paintMenuItem(g2, x, y, s);
        }

        g2.dispose();
    }

    // ==================== IMPLEMENTATIONS ====================

    private void paintAdd(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 4, y + s / 2, x + s - 4, y + s / 2);
        g2.drawLine(x + s / 2, y + 4, x + s / 2, y + s - 4);
    }

    private void paintEdit(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 3, y + s - 5, x + s - 7, y + 5);
        g2.drawLine(x + 5, y + s - 3, x + s - 5, y + 7);
        g2.drawLine(x + s - 7, y + 5, x + s - 5, y + 7);
        g2.drawLine(x + 3, y + s - 5, x + 5, y + s - 3);
    }

    private void paintDelete(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x + 4, y + 5, s - 8, s - 9);
        g2.drawLine(x + 2, y + 5, x + s - 2, y + 5);
        g2.drawRect(x + 6, y + 3, s - 12, 2);
    }

    private void paintRefresh(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(x + 3, y + 3, s - 6, s - 6, 45, 270);
        g2.drawLine(x + s - 5, y + s / 2, x + s - 5, y + 4);
        g2.drawLine(x + s - 5, y + s / 2, x + s - 9, y + s / 2);
    }

    private void paintSearch(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 3, y + 3, s - 9, s - 9);
        g2.drawLine(x + s - 7, y + s - 7, x + s - 2, y + s - 2);
    }

    private void paintHeader(Graphics2D g2, int x, int y, int s) {
        Color gold = GOLD_ACCENT != null ? GOLD_ACCENT : new Color(212, 175, 55);
        Color primary = PRIMARY_COLOR != null ? PRIMARY_COLOR : new Color(30, 41, 59);

        g2.setColor(gold);
        int[] px = { x + s / 2, x + s, x + s / 2, x };
        int[] py = { y, y + s / 2, y + s, y + s / 2 };
        g2.fillPolygon(px, py, 4);

        g2.setColor(primary);
        g2.fillOval(x + s / 3, y + s / 3, s / 3, s / 3);
    }

    private void paintCheck(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 3, y + s / 2, x + s / 2 - 2, y + s - 4);
        g2.drawLine(x + s / 2 - 2, y + s - 4, x + s - 2, y + 3);
    }

    private void paintCross(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color != null ? color : Color.BLACK);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 3, y + 3, x + s - 3, y + s - 3);
        g2.drawLine(x + s - 3, y + 3, x + 3, y + s - 3);
    }

    private void paintUser(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillOval(x + s / 4, y, s / 2, s / 2);
        g2.fill(new Arc2D.Double(x, y + s / 2, s, s * 0.7, 0, 180, Arc2D.CHORD));

        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x + s / 4, y, s / 2, s / 2);
    }

    private void paintMoney(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillRoundRect(x, y + s / 4, s, s / 2, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, s / 2));
        drawCenteredString(g2, "$", new Rectangle(x, y + s / 4, s, s / 2));
    }

    private void paintCalendar(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillRoundRect(x, y + 4, s, s - 4, 8, 8);

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(x, y + 4, s, 6, 8, 8);

        g2.setColor(Color.WHITE);
        int dotSize = s / 5;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                g2.fillOval(x + 4 + i * (dotSize + 2), y + 12 + j * (dotSize + 2), dotSize, dotSize);
            }
        }
    }

    private void paintCheckIn(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillOval(x, y, s, s);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + s / 4, y + s / 2, x + s / 2, y + s * 3 / 4);
        g2.drawLine(x + s / 2, y + s * 3 / 4, x + s * 3 / 4, y + s / 4);
    }

    private void paintCheckOut(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillOval(x, y, s, s);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + s / 4, y + s / 4, x + s * 3 / 4, y + s * 3 / 4);
        g2.drawLine(x + s * 3 / 4, y + s / 4, x + s / 4, y + s * 3 / 4);
    }

    private void paintTag(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        int[] px = { x, x + s - 8, x + s, x + 8 };
        int[] py = { y + s / 2, y, y + s / 2, y + s };
        g2.fillPolygon(px, py, 4);

        g2.setColor(Color.WHITE);
        g2.fillOval(x + s - 10, y + s / 2 - 2, 4, 4);
    }

    private void paintCard(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.fillRoundRect(x, y + 2, s, s * 2 / 3, 6, 6);

        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRect(x + 2, y + 6, s - 4, 3);
    }

    private void paintLock(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(x + s * 0.2, y, s * 0.6, s * 0.6, 5, 5));
        g2.fill(new RoundRectangle2D.Double(x, y + s * 0.4, s, s * 0.6, 3, 3));
    }

    private void paintEyeOpen(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        drawEyeBase(g2, x, y, s);
        g2.fill(new Ellipse2D.Double(x + s / 3.0, y + s / 3.0, s / 3.0, s / 3.0));
    }

    private void paintEyeClose(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        drawEyeBase(g2, x, y, s);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x + 2, y + 2, x + s - 2, y + s - 2);
    }

    private void drawEyeBase(Graphics2D g2, int x, int y, int s) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + s / 2.0);
        p.curveTo(x + s / 4.0, y, x + s * 3 / 4.0, y, x + s, y + s / 2.0);
        p.curveTo(x + s * 3 / 4.0, y + s, x + s / 4.0, y + s, x, y + s / 2.0);
        g2.draw(p);
    }

    private void paintStar(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        // Tọa độ chuẩn cho size 20
        double[] px = { 10, 13, 20, 15, 17, 10, 3, 5, 0, 7 };
        double[] py = { 0, 7, 7, 12, 20, 15, 20, 12, 7, 7 };

        double scale = s / 20.0;
        Path2D p = new Path2D.Double();
        p.moveTo(x + px[0] * scale, y + py[0] * scale);
        for (int i = 1; i < px.length; i++) {
            p.lineTo(x + px[i] * scale, y + py[i] * scale);
        }
        p.closePath();
        g2.fill(p);
    }

    private void paintMenuItem(Graphics2D g2, int x, int y, int s) {
        g2.setColor(color);
        int circleSize = s - 2;
        g2.fillOval(x, y + 1, circleSize, circleSize);

        if (symbol != null && !symbol.isEmpty()) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (s * 0.6)));
            drawCenteredString(g2, symbol, new Rectangle(x, y + 1, circleSize, circleSize));
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
        FontMetrics fm = g2.getFontMetrics();
        int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    // ==================== GLOBAL COLORS ====================

    private static Color GOLD_ACCENT;
    private static Color PRIMARY_COLOR;

    public static void setGlobalColors(Color gold, Color primary) {
        GOLD_ACCENT = gold;
        PRIMARY_COLOR = primary;
    }
}