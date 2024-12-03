package com.sk.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${spring.jwt.secret}") String jwtSecret;
    private final long accessTokenExpirationInMs = 24 * 60 * 60 * 1000L; // access_token 유효 기간 1일
    private final long refreshTokenExpirationInMs = 30 * 24 * 60 * 60 * 1000L; // refresh_token 유효 기간 30일

    // HTTP 요청 헤더에서 JWT 토큰을 추출하는 메서드
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 이후의 토큰 부분만 반환
        }
        return null;
    }

    // Access Token을 발급하는 메서드
    public String createAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationInMs))
                .claim("role", "USER")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Refresh Token을 발급하는 메서드
    public String createRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // 토큰을 파싱하여 Claims 객체를 반환하는 메서드
    public Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    // 토큰에서 사용자의 이메일을 추출하는 메서드
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰이 만료되었는지 확인하는 메서드
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    // 토큰의 만료 날짜를 추출하는 메서드
    private Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

}
