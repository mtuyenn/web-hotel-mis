package com.hospitality.mis.auth.application;

import com.hospitality.mis.auth.api.AuthDtos;
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

@Service
public class JwtTokenService {
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final JwtEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public JwtTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public IssuedTokens issue(String employeeId, Collection<? extends GrantedAuthority> authorities,
                              String familyId) {
        Instant issuedAt = Instant.now();
        List<String> roles = authorities == null ? List.of() : authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(employeeId)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ACCESS_TOKEN_TTL))
                .id(UUID.randomUUID().toString())
                .claim("token_type", "access")
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

    public String generateFamilyId() {
        return UUID.randomUUID().toString();
    }

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

    private String generateRefreshToken() {
        byte[] value = new byte[32];
        random.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public record IssuedTokens(AuthDtos.TokenResponse response, Instant issuedAt,
                               Instant refreshExpiresAt, String refreshTokenHash) {
    }
}
