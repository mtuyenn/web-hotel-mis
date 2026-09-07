package com.hospitality.mis.common.actor;

import java.util.Objects;

/** The authenticated identity responsible for an application operation. */
public record ActorId(String value) {
    public static final String SYSTEM_VALUE = "SYSTEM";

    public ActorId {
        value = Objects.requireNonNull(value, "actor id must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("actor id must not be blank");
        if (value.length() > 50) throw new IllegalArgumentException("actor id must not exceed 50 characters");
    }

    public static ActorId system() { return new ActorId(SYSTEM_VALUE); }
}
