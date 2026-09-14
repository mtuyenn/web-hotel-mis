/* Enum này giới hạn vòng đời phiếu bảo trì vào các trạng thái hợp lệ. */
package com.hospitality.mis.entity.operations;

/** Các trạng thái hợp lệ trong vòng đời phiếu bảo trì phòng. */
public enum MaintenanceStatus {
    /** Phiếu mới tạo, chưa bắt đầu xử lý. */
    CHUA_XU_LY,
    /** Công việc đang được kỹ thuật viên thực hiện. */
    DANG_BAO_TRI,
    /** Công việc đã hoàn tất. */
    DA_HOAN_THANH
}
