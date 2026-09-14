package com.hospitality.mis.dao.room;
import com.hospitality.mis.entity.room.RoomStatus;




import jakarta.persistence.AttributeConverter;

import jakarta.persistence.Converter;



/** Lưu trạng thái phòng chuẩn hóa bằng các mã enum hiện có trong cơ sở dữ liệu. */

@Converter(autoApply = false)

public class RoomStatusConverter implements AttributeConverter<RoomStatus, String> {

    @Override

    /** Ghi null thành null; trạng thái khác dùng mã cơ sở dữ liệu chuẩn của enum. */
    public String convertToDatabaseColumn(RoomStatus status) {

        return status == null ? null : status.databaseCode();

    }



    @Override

    /** Đọc null thành null; mã khác được trim, viết hoa và giải mã từ mã DB hoặc tên enum; mã lạ bị từ chối. */
    public RoomStatus convertToEntityAttribute(String databaseValue) {

        return databaseValue == null ? null : RoomStatus.fromDatabaseCode(databaseValue);

    }

}
