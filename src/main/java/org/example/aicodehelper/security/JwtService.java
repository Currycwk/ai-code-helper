package org.example.aicodehelper.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.aicodehelper.config.AppSecurityProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 服务。
 * 负责生成、解析和校验 JWT Token，
 * 是登录后签发令牌以及后续请求中识别用户身份的核心组件。
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationHours;

    public JwtService(AppSecurityProperties properties) {
        this.key = buildKey(properties.getJwtSecret());
        this.expirationHours = properties.getExpirationHours();
    }

    public String generateToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim("username", principal.getUsername())
                .claim("role", principal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    private SecretKey buildKey(String secret) {
        if (secret != null && secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() >= 44) {
            try {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
