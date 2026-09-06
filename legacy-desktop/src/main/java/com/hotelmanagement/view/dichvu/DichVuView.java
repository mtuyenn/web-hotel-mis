package com.hotelmanagement.view.dichvu;

import com.hotelmanagement.controller.DichVuController;
import com.hotelmanagement.model.dto.DichVuDTO;
import com.hotelmanagement.view.BaseView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Lớp DichVuView - Màn hình quản lý dịch vụ
 * 
 * Chức năng:
 * - Hiển thị danh sách tất cả dịch vụ
 * - Tìm kiếm dịch vụ theo mã hoặc tên
 * - Thêm dịch vụ mới
 * - Sửa thông tin dịch vụ
 * - Xóa dịch vụ
 * - Tự động tải dữ liệu từ cơ sở dữ liệu
 * 
 * Thiết kế:
 * - Kế thừa từ BaseView để sử dụng chung các thành phần (thanh tìm kiếm, nút
 * chức năng)
 * - Sử dụng JTable để hiển thị danh sách dịch vụ
 * - Sử dụng SwingWorker để tải dữ liệu bất đồng bộ
 * - Sử dụng màu sắc hiện đại, dễ nhìn
 * - Tự động điều chỉnh hiển thị theo chế độ thống kê
 */
public class DichVuView extends BaseView {

    private final DichVuController controller = new DichVuController();
    private List<DichVuDTO> currentList;

    public DichVuView() {
        super();
        setName("Dịch Vụ");
        searchPlaceholder = "Nhập mã hoặc tên dịch vụ để tìm...";

        // Khởi tạo bảng
        String[] columns = { "Mã Dịch Vụ", "Tên Dịch Vụ", "Giá Dịch Vụ" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Sự kiện các nút chức năng
        btnAdd.addActionListener(e -> addNew());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());
        btnSearch.addActionListener(e -> search());

        loadData();
    }

    @Override
    protected void loadData() {
        new SwingWorker<List<DichVuDTO>, Void>() {
            @Override
            protected List<DichVuDTO> doInBackground() {
                return controller.layTatCaDichVu();
            }

            @Override
            protected void done() {
                try {
                    currentList = get();
                    model.setRowCount(0);
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    for (DichVuDTO dv : currentList) {
                        String gia = format.format(dv.getGiaDV());
                        model.addRow(new Object[] {
                                dv.getMaDV(), dv.getTenDichVu(), gia
                        });
                    }
                } catch (Exception ex) {
                    showError("Lỗi tải dữ liệu dịch vụ: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    protected void addNew() {
        DichVuDialog dialog = new DichVuDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Dịch Vụ Mới",
                null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadData();
        }
    }

    @Override
    protected void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một dịch vụ để sửa");
            return;
        }
        String maDV = (String) model.getValueAt(row, 0);
        DichVuDTO dto = controller.timDichVuTheoMa(maDV);
        if (dto != null) {
            DichVuDialog dialog = new DichVuDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Cập Nhật Dịch Vụ", dto);
            dialog.setVisible(true);
            if (dialog.isSuccess()) {
                loadData();
            }
        }
    }

    @Override
    protected void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một dịch vụ để xóa");
            return;
        }
        String maDV = (String) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa dịch vụ " + name + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.xoaDichVu(maDV);
                showSuccess("Xóa thành công!");
                loadData();
            } catch (Exception ex) {
                showError("Lỗi xóa dịch vụ: " + ex.getMessage());
            }
        }
    }

    @Override
    protected void search() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty() && currentList != null) {
            loadData();
            return;
        }

        if (currentList != null) {
            model.setRowCount(0);
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            for (DichVuDTO dv : currentList) {
                if ((dv.getTenDichVu() != null && dv.getTenDichVu().toLowerCase().contains(query)) ||
                        (dv.getMaDV() != null && dv.getMaDV().toLowerCase().contains(query))) {
                    String gia = format.format(dv.getGiaDV());
                    model.addRow(new Object[] {
                            dv.getMaDV(), dv.getTenDichVu(), gia
                    });
                }
            }
        }
    }
}
