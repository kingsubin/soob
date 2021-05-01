package com.community.soob.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    public final static long TOKEN_VALIDATION_SECOND = 1000L * 10;
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000L * 60 * 24 * 2;

    final static public String ACCESS_TOKEN_NAME = "accessToken";
    final static public String REFRESH_TOKEN_NAME = "refreshToken";

    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰이 유효한 토큰인지 검사한 후, 토큰에 담긴 Payload 값을 가져온다.
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 추출한 Payload 로부터 accountEmail 을 가져온다.
    public String getAccountEmail(String token) {
        return extractAllClaims(token).get("accountEmail", String.class);
    }

    // 토큰이 만료됐는지 안됐는지 확인.
    public Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // Access Token 생성
    public String generateToken(String accountEmail) {
        return doGenerateToken(accountEmail, TOKEN_VALIDATION_SECOND);
    }

    // Refresh Token 생성
    public String generateRefreshToken(String accountEmail) {
        return doGenerateToken(accountEmail, REFRESH_TOKEN_VALIDATION_SECOND);
    }

    // 토큰을 생성, 페이로드에 담길 값은 accountEmail
    public String doGenerateToken(String accountEmail, long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("accountEmail", accountEmail);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String accountEmail = getAccountEmail(token);
        return (accountEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
