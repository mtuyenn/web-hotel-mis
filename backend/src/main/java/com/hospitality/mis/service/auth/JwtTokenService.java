package com.hospitality.mis.service.auth;





import com.hospitality.mis.dto.auth.AuthDtos;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import org.springframework.security.oauth2.jwt.JwtEncoder;

import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.security.oauth2.jwt.JwsHeader;

import org.springframework.stereotype.Service;



import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;

import java.time.Duration;

import java.time.Instant;

import java.util.Base64;

import java.util.Collection;

import java.util.List;

import java.util.UUID;



/** Chính sách về thời hạn, nội dung và mã hóa access/refresh token JWT. */
@Service

public class JwtTokenService {

    /** Thời hạn access token ngắn để giới hạn tác động khi token bị lộ. */
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    /** Thời hạn refresh token cho phép duy trì phiên đăng nhập. */
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);



    /** Bộ mã hóa JWT được cấu hình bởi lớp bảo mật ứng dụng. */
    private final JwtEncoder encoder;

    /** Nguồn ngẫu nhiên mật mã cho refresh token và family id. */
    private final SecureRandom random = new SecureRandom();



    public JwtTokenService(JwtEncoder encoder) {

        this.encoder = encoder;

    }



    /** Tạo cặp token cho principal và giữ nguyên family khi refresh luân chuyển. */
    public IssuedTokens issue(PrincipalType principalType, String principalId,

                              Collection<? extends GrantedAuthority> authorities, String familyId) {

        if (principalType == null || principalId == null || principalId.isBlank()) {

            throw new IllegalArgumentException("JWT principal type and id are required");

        }

        if (familyId == null || familyId.isBlank()) {

            throw new IllegalArgumentException("JWT family id is required");

        }

        Instant issuedAt = Instant.now();

        List<String> roles = authorities == null ? List.of() : authorities.stream()

                .map(GrantedAuthority::getAuthority)

                .filter(authority -> authority != null && authority.startsWith("ROLE_"))

                .map(authority -> authority.substring("ROLE_".length()))

                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()

                .subject(principalId)

                .issuedAt(issuedAt)

                .expiresAt(issuedAt.plus(ACCESS_TOKEN_TTL))

                .id(UUID.randomUUID().toString())

                .claim("token_type", "access")

                .claim("principal_type", principalType.name())

                .claim("principal_id", principalId)
                .claim("session_id", familyId)

                .claim("roles", roles)

                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)

                .type("JWT")

                .keyId("hotel-mis")

                .build();

        String accessToken = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        String refreshToken = generateRefreshToken();

        return new IssuedTokens(

                new AuthDtos.TokenResponse(accessToken, refreshToken, "Bearer",

                        ACCESS_TOKEN_TTL.toSeconds(), REFRESH_TOKEN_TTL.toSeconds()),

                issuedAt, issuedAt.plus(REFRESH_TOKEN_TTL), hash(refreshToken));

    }



    /** Tạo mã ngẫu nhiên dùng để thu hồi cả một họ refresh token. */
    public String generateFamilyId() {

        return UUID.randomUUID().toString();

    }



    /** Băm token trước khi lưu hoặc tra cứu, tránh lưu secret dạng rõ trong DB. */
    public static String hash(String token) {

        try {

            byte[] digest = MessageDigest.getInstance("SHA-256")

                    .digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder(digest.length * 2);

            for (byte value : digest) {

                result.append(Character.forDigit((value >>> 4) & 0x0f, 16));

                result.append(Character.forDigit(value & 0x0f, 16));

            }

            return result.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException("SHA-256 is not available", exception);

        }

    }



    /** Sinh refresh token URL-safe từ nguồn ngẫu nhiên mật mã. */
    private String generateRefreshToken() {

        byte[] value = new byte[32];

        random.nextBytes(value);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);

    }



    public record IssuedTokens(AuthDtos.TokenResponse response, Instant issuedAt,

                               Instant refreshExpiresAt, String refreshTokenHash) {

    }

    public enum PrincipalType {
        EMPLOYEE, CUSTOMER
    }

}
