package com.hotelmanagement.view.baocao;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

/**
 * Lớp FilterDashboardPanel - Panel chứa bộ lọc thời gian cho các báo cáo
 * 
 * Chức năng:
 * - Cho phép chọn chế độ lọc: Ngày, Tháng, Năm
 * - Hiển thị các thành phần tương ứng với chế độ đã chọn
 * - Tải dữ liệu khi nhấn nút "Áp dụng"
 * 
 * Thiết kế:
 * - Giao diện dạng FlowLayout với các thành phần trên một hàng
 * - Sử dụng JSpinner cho chế độ Ngày
 * - Sử dụng JComboBox cho Tháng và Năm
 * - Có hiệu ứng hover cho nút Áp dụng
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 */
public class FilterDashboardPanel extends JPanel {

    public enum FilterMode {
        DAY, MONTH, YEAR
    }

    private final Runnable onApplyCallback;

    private JComboBox<FilterMode> cboMode;
    private JSpinner dateSpinner; // cho chế độ Day
    private JComboBox<Integer> cboMonth;
    private JComboBox<Integer> cboYear;

    public FilterDashboardPanel(Runnable onApply) {
        this.onApplyCallback = onApply;
        initUI();
    }

    private void initUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(15, 0, 15, 0));

        add(new JLabel("Bộ lọc thời gian:"));

        // Chọn chế độ
        cboMode = new JComboBox<>(FilterMode.values());
        cboMode.setSelectedItem(FilterMode.MONTH);
        cboMode.addActionListener(e -> updateComponentVisibility());
        add(cboMode);

        // Ngày cụ thể
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(editor);
        dateSpinner.setPreferredSize(new Dimension(130, 32));
        add(dateSpinner);

        // Tháng
        add(new JLabel("Tháng:"));
        cboMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++)
            cboMonth.addItem(i);
        cboMonth.setSelectedItem(LocalDate.now().getMonthValue());
        cboMonth.setPreferredSize(new Dimension(70, 32));
        add(cboMonth);

        // Năm
        add(new JLabel("Năm:"));
        cboYear = new JComboBox<>();
        int current = LocalDate.now().getYear();
        for (int y = current - 10; y <= current; y++) {
            cboYear.addItem(y);
        }
        cboYear.setSelectedItem(current);
        cboYear.setPreferredSize(new Dimension(80, 32));
        add(cboYear);

        // Nút Áp dụng
        JButton btnApply = new JButton("Áp dụng");
        btnApply.setBackground(new Color(15, 23, 42));
        btnApply.setForeground(Color.WHITE);
        btnApply.setFocusPainted(false);
        btnApply.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnApply.addActionListener(e -> {
            if (onApplyCallback != null) {
                onApplyCallback.run();
            }
        });
        add(btnApply);

        updateComponentVisibility();
    }

    private void updateComponentVisibility() {
        FilterMode mode = (FilterMode) cboMode.getSelectedItem();
        boolean isDay = mode == FilterMode.DAY;

        dateSpinner.setVisible(isDay);
        cboMonth.setVisible(mode == FilterMode.MONTH || mode == FilterMode.YEAR);

        revalidate();
        repaint();
    }

    // ================== Getter ==================
    public LocalDate getStartDate() {
        FilterMode mode = (FilterMode) cboMode.getSelectedItem();
        int year = (int) cboYear.getSelectedItem();

        if (mode == FilterMode.DAY) {
            java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
            return utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } else if (mode == FilterMode.MONTH) {
            int month = (int) cboMonth.getSelectedItem();
            return LocalDate.of(year, month, 1);
        } else { // YEAR
            return LocalDate.of(year, 1, 1);
        }
    }

    public LocalDate getEndDate() {
        FilterMode mode = (FilterMode) cboMode.getSelectedItem();
        int year = (int) cboYear.getSelectedItem();

        if (mode == FilterMode.DAY) {
            return getStartDate();
        } else if (mode == FilterMode.MONTH) {
            int month = (int) cboMonth.getSelectedItem();
            return LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        } else {
            return LocalDate.of(year, 12, 31);
        }
    }

    public FilterMode getMode() {
        return (FilterMode) cboMode.getSelectedItem();
    }
}