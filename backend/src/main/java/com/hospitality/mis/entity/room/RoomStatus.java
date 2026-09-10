package com.hospitality.mis.entity.room;



import com.fasterxml.jackson.annotation.JsonValue;


import java.util.Locale;



/**

 * Canonical room state.  The database uses the existing Vietnamese codes;

 * those codes are retained at the JSON boundary for the existing API

 * contract, while application code uses this enum.

 */

public enum RoomStatus {

    READY("SAN_SANG", false),

    OCCUPIED("DANG_O", false),

    CLEANING("DANG_DON_DEP", true),

    MAINTENANCE("BAO_TRI", true),

    OUT_OF_SERVICE("NGUNG_SU_DUNG", true),

    RESERVED("DA_DAT", false),

    RETURNED("DA_TRA", false),

    CANCELLED("DA_HUY", false);



    private final String databaseCode;

    private final boolean blocksAvailability;



    RoomStatus(String databaseCode, boolean blocksAvailability) {

        this.databaseCode = databaseCode;

        this.blocksAvailability = blocksAvailability;

    }



    @JsonValue

    public String databaseCode() {

        return databaseCode;

    }



    public boolean blocksAvailability() {

        return blocksAvailability;

    }



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



    public static RoomStatus fromApiValue(String value) {
        return fromDatabaseCode(value);
    }
}
