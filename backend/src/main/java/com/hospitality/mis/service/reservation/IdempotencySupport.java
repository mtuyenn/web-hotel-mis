package com.hospitality.mis.service.reservation;

import com.hospitality.mis.common.exception.DomainException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Command idempotency for the reservation and operations application services.
 *
 * <p>The canonical schema currently persists a key only for reservation
 * creation. Other operation tables have no key column and cannot be changed in
 * this slice, so their completed command results are retained by this
 * application service instance. The key is always bound to the authenticated
 * actor and request fingerprint.</p>
 */
public final class IdempotencySupport {
    private final Map<String, Entry> completed = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized <T> T execute(String scope, String key, String actor,
                                       String fingerprint, Supplier<T> command) {
        String normalizedKey = requireKey(key);
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(fingerprint, "fingerprint");
        String mapKey = scope + "|" + normalizedKey;
        Entry previous = completed.get(mapKey);
        if (previous != null) {
            if (!previous.actor().equals(actor) || !previous.fingerprint().equals(fingerprint)) {
                throw new DomainException("IDEMPOTENCY_KEY_CONFLICT",
                        "Idempotency key đã được dùng cho yêu cầu khác hoặc actor khác");
            }
            return (T) previous.result();
        }
        T result = command.get();
        completed.put(mapKey, new Entry(actor, fingerprint, result));
        return result;
    }

    public static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new DomainException("IDEMPOTENCY_KEY_REQUIRED", "Thiếu Idempotency-Key");
        }
        String normalized = key.trim();
        if (normalized.length() > 100) {
            throw new DomainException("INVALID_IDEMPOTENCY_KEY", "Idempotency-Key không được vượt quá 100 ký tự");
        }
        return normalized;
    }

    public static String fingerprint(String canonicalRequest) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required", impossible);
        }
    }

    private record Entry(String actor, String fingerprint, Object result) {}
}
