package com.hospitality.mis.entity.room;



import com.fasterxml.jackson.annotation.JsonValue;


import java.util.Locale;



/**

 * Trạng thái phòng chuẩn. Cơ sở dữ liệu sử dụng các mã tiếng Việt hiện có;

 * các mã này được giữ nguyên ở ranh giới JSON để duy trì

 * hợp đồng API hiện có, trong khi mã ứng dụng sử dụng enum này.

 */

public enum RoomStatus {

    /** Phòng sẵn sàng nhận khách. */
    READY("SAN_SANG", false),

    /** Phòng đang có khách ở. */
    OCCUPIED("DANG_O", false),

    /** Phòng đang được dọn và tạm thời không phân bổ. */
    CLEANING("DANG_DON_DEP", true),

    /** Phòng đang bảo trì và không phân bổ. */
    MAINTENANCE("BAO_TRI", true),

    /** Phòng ngừng sử dụng và không phân bổ. */
    OUT_OF_SERVICE("NGUNG_SU_DUNG", true),

    /** Phòng đã được giữ cho một đặt phòng. */
    RESERVED("DA_DAT", false),

    /** Phòng đã trả theo trạng thái nghiệp vụ hiện tại. */
    RETURNED("DA_TRA", false),

    /** Phòng/đặt phòng đã bị hủy. */
    CANCELLED("DA_HUY", false);



    /** Mã tiếng Việt tương thích với dữ liệu lưu trữ/API hiện hành. */
    private final String databaseCode;

    /** Cho biết trạng thái có chặn phân bổ phòng hay không. */
    private final boolean blocksAvailability;



    /** Gắn mã lưu trữ và cờ khả dụng cho từng trạng thái. */
    RoomStatus(String databaseCode, boolean blocksAvailability) {

        this.databaseCode = databaseCode;

        this.blocksAvailability = blocksAvailability;

    }



    @JsonValue

    public String databaseCode() {

        return databaseCode;

    }



    /** Trả về true nếu không được chọn phòng cho đặt phòng mới. */
    public boolean blocksAvailability() {

        return blocksAvailability;

    }

    /** Only these six values may be stored as the current operational room state. */
    public boolean isOperationalStatus() {
        return this != RETURNED && this != CANCELLED;
    }



    /** Đổi mã DB/API hoặc tên enum sang trạng thái chuẩn, fail-fast nếu không hợp lệ. */
    public static RoomStatus fromDatabaseCode(String value) {

        if (value == null) {

            throw new IllegalArgumentException("room status must not be null");

        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        for (RoomStatus status : values()) {

            if (status.databaseCode.equals(normalized) || status.name().equals(normalized)) {

                return status;

            }

        }

        throw new IllegalArgumentException("Unknown room status: " + value);

    }



    /** Đọc giá trị từ API theo cùng hợp đồng mã với cơ sở dữ liệu. */
    public static RoomStatus fromApiValue(String value) {
        return fromDatabaseCode(value);
    }
}
