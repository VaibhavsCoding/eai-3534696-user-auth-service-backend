package com.user.authentication.security;

import com.user.authentication.repository.LoginHistoryRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;
    private final LoginHistoryRepository loginHistoryRepository;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs, LoginHistoryRepository loginHistoryRepository) {
        // secret should be base64 encoded or long enough; use Keys.hmacShaKeyFor
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public String generateToken(String subject, Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject) // typically user id or email
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);

        if (additionalClaims != null) builder.addClaims(additionalClaims);

        return builder.compact();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String getSubject(String token) {
        return validateToken(token).getBody().getSubject();
    }
}
