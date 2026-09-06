package com.hotelmanagement.view.phong;

import com.hotelmanagement.controller.DichVuController;
import com.hotelmanagement.model.dto.ChiTietDichVuDTO;
import com.hotelmanagement.model.dto.DichVuDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp ServiceDialog là một JDialog tùy chỉnh dùng để hiển thị danh sách dịch vụ
 * và cho phép người dùng chọn dịch vụ để thêm vào đơn đặt phòng.
 * 
 * Chức năng:
 * - Hiển thị danh sách dịch vụ dưới dạng lưới (grid)
 * - Hiển thị giỏ hàng chứa các dịch vụ đã chọn
 * - Cho phép thêm dịch vụ vào giỏ hàng
 * - Cho phép xóa dịch vụ khỏi giỏ hàng
 * - Cho phép thay đổi số lượng dịch vụ
 * - Tính tổng tiền các dịch vụ trong giỏ hàng
 * - Lưu các dịch vụ đã chọn vào cơ sở dữ liệu
 * - Tải danh sách dịch vụ đã có sẵn cho đơn đặt phòng
 * 
 * Thiết kế:
 * - Sử dụng JSplitPane để chia thành 2 cột: danh sách dịch vụ (trái) và giỏ
 * hàng (phải)
 * - Sử dụng ServiceCard để hiển thị từng dịch vụ
 * - Sử dụng JTable để hiển thị giỏ hàng
 * - Sử dụng SwingWorker để tải dữ liệu bất đồng bộ, tránh làm treo giao diện
 * - Có hiệu ứng hover cho các nút và thẻ dịch vụ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 */
public class ServiceDialog extends JDialog {
    private final Long maDP;
    private final DichVuController dvController = new DichVuController();
    private final List<ChiTietDichVuDTO> cartItems = new ArrayList<>();
    private final Runnable onSuccess;

    private JPanel pnlServiceGrid;
    private JTable tblCart;
    private DefaultTableModel modelCart;
    private JLabel lblTotal;

    private final Color PRIMARY_COLOR = new Color(30, 41, 59);
    private final Color GREEN_ACCENT = new Color(46, 204, 113);
    private final Color DB_BG = new Color(245, 247, 250);

    public ServiceDialog(Dialog owner, Long maDP, Runnable onSuccess) {
        super(owner, "Dịch Vụ - Mã Đơn: " + maDP, true);
        this.maDP = maDP;
        this.onSuccess = onSuccess;

        setSize(1100, 750);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(DB_BG);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setLeftComponent(createServiceBrowser());
        splitPane.setRightComponent(createCartPanel());
        add(splitPane, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);

        // Load existing services for this maDP
        loadExistingServices();
    }

    /**
     * Tạo panel chứa danh sách dịch vụ
     * -------------------------------------------------------------------------
     * 
     * @return JPanel chứa danh sách dịch vụ
     */
    private JPanel createServiceBrowser() {
        JPanel pnlLeft = new JPanel(new BorderLayout(10, 10));
        pnlLeft.setBackground(DB_BG);
        pnlLeft.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH DỊCH VỤ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_COLOR);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        pnlLeft.add(pnlHeader, BorderLayout.NORTH);

        pnlServiceGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        pnlServiceGrid.setBackground(Color.WHITE);
        pnlServiceGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(pnlServiceGrid);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pnlLeft.add(scroll, BorderLayout.CENTER);

        // Load services worker
        new SwingWorker<List<DichVuDTO>, Void>() {
            @Override
            protected List<DichVuDTO> doInBackground() {
                return dvController.layTatCaDichVu();
            }

            @Override
            protected void done() {
                try {
                    List<DichVuDTO> services = get();
                    for (DichVuDTO dv : services) {
                        pnlServiceGrid.add(new ServiceCard(dv, ServiceDialog.this::addToCart));
                    }
                    pnlServiceGrid.revalidate();
                    pnlServiceGrid.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        return pnlLeft;
    }

    /**
     * Tạo panel chứa giỏ hàng
     * -------------------------------------------------------------------------
     * 
     * @return JPanel chứa giỏ hàng
     */
    private JPanel createCartPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout(10, 10));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("DỊCH VỤ ĐÃ CHỌN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_COLOR);
        pnlRight.add(lblTitle, BorderLayout.NORTH);

        String[] cols = { "Dịch Vụ", "Giá", "SL", "Thành Tiền", "Xóa" };
        modelCart = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // Allow quantity edit
            }
        };

        tblCart = new JTable(modelCart);
        tblCart.setRowHeight(40);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Custom renderers
        tblCart.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tblCart.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        pnlRight.add(new JScrollPane(tblCart), BorderLayout.CENTER);

        JPanel pnlSummary = new JPanel(new BorderLayout());
        pnlSummary.setOpaque(false);
        pnlSummary.setBorder(new EmptyBorder(10, 0, 0, 0));

        lblTotal = new JLabel("Tổng cộng: 0 VNĐ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(new Color(231, 76, 60));
        pnlSummary.add(lblTotal, BorderLayout.EAST);

        pnlRight.add(pnlSummary, BorderLayout.SOUTH);

        return pnlRight;
    }

    /**
     * Tạo panel chứa footer với các nút hành động
     * -------------------------------------------------------------------------
     * 
     * @return JPanel chứa footer
     */
    private JPanel createFooter() {
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton btnRefresh = new JButton("LÀM MỚI");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> {
            cartItems.clear();
            refreshCartTable();
        });

        JButton btnConfirm = new JButton("XÁC NHẬN");
        btnConfirm.setBackground(GREEN_ACCENT);
        btnConfirm.setForeground(Color.BLACK);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setPreferredSize(new Dimension(180, 40));
        btnConfirm.addActionListener(e -> saveServiceUsage());

        JButton btnClose = new JButton("ĐÓNG");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dispose());

        pnlFooter.add(btnRefresh);
        pnlFooter.add(btnClose);
        pnlFooter.add(btnConfirm);

        return pnlFooter;
    }

    /**
     * Thêm dịch vụ vào giỏ hàng
     * -------------------------------------------------------------------------
     * 
     * @param dv Đối tượng DichVuDTO chứa thông tin dịch vụ cần thêm
     */
    private void addToCart(DichVuDTO dv) {
        // Check if exists
        for (ChiTietDichVuDTO item : cartItems) {
            if (item.getMaDV().equals(dv.getMaDV())) {
                item.setSoLuong(item.getSoLuong() + 1);
                item.setThanhTien(item.getGiaDV().multiply(BigDecimal.valueOf(item.getSoLuong())));
                refreshCartTable();
                return;
            }
        }

        // Add new
        ChiTietDichVuDTO newItem = new ChiTietDichVuDTO(maDP, dv.getMaDV(), dv.getTenDichVu(), dv.getGiaDV(),
                LocalDate.now(), 1);
        cartItems.add(newItem);
        refreshCartTable();
    }

    /**
     * Tải danh sách dịch vụ đã sử dụng cho đơn đặt phòng
     * -------------------------------------------------------------------------
     * Sử dụng SwingWorker để tải dữ liệu bất đồng bộ, tránh làm treo giao diện
     */
    private void loadExistingServices() {
        new SwingWorker<List<ChiTietDichVuDTO>, Void>() {
            @Override
            protected List<ChiTietDichVuDTO> doInBackground() {
                return dvController.layDichVuDaDung(maDP);
            }

            @Override
            protected void done() {
                try {
                    cartItems.clear();
                    cartItems.addAll(get());
                    refreshCartTable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Cập nhật lại bảng giỏ hàng
     * -------------------------------------------------------------------------
     * Xóa tất cả các dòng hiện tại và thêm lại từ danh sách cartItems
     * Tính toán và hiển thị tổng tiền
     */
    private void refreshCartTable() {
        modelCart.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;

        for (ChiTietDichVuDTO item : cartItems) {
            total = total.add(item.getThanhTien());
            modelCart.addRow(new Object[] {
                    item.getTenDichVu(),
                    String.format("%,.0f", item.getGiaDV()),
                    item.getSoLuong(),
                    String.format("%,.0f", item.getThanhTien()),
                    "Xóa"
            });
        }
        lblTotal.setText("Tổng cộng: " + String.format("%,.0f", total) + " VNĐ");
    }

    private void saveServiceUsage() {
        try {
            dvController.luuDichVuSuDung(maDP, cartItems);
            JOptionPane.showMessageDialog(this, "Đã cập nhật dịch vụ thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null)
                onSuccess.run();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Table Helper Classes
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(231, 76, 60));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                cartItems.remove(row);
                refreshCartTable();
            }
            isPushed = false;
            return label;
        }
    }
}
