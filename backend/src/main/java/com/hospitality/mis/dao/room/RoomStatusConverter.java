package com.hospitality.mis.dao.room;
import com.hospitality.mis.entity.room.RoomStatus;




import jakarta.persistence.AttributeConverter;

import jakarta.persistence.Converter;



/** Persists canonical room status using the existing database enum codes. */

@Converter(autoApply = false)

public class RoomStatusConverter implements AttributeConverter<RoomStatus, String> {

    @Override

    public String convertToDatabaseColumn(RoomStatus status) {

        return status == null ? null : status.databaseCode();

    }



    @Override

    public RoomStatus convertToEntityAttribute(String databaseValue) {

        return databaseValue == null ? null : RoomStatus.fromDatabaseCode(databaseValue);

    }

}
