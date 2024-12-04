package com.sk.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${spring.jwt.secret}")
    String jwtSecret;
    private final long accessTokenExpirationInMs = 24 * 60 * 60 * 1000L; // access_token 유효 기간 1일
    private final long refreshTokenExpirationInMs = 30 * 24 * 60 * 60 * 1000L; // refresh_token 유효 기간 30일

    // HTTP 요청 헤더에서 access token을 추출하는 메서드
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 이후의 토큰 부분만 반환
        }
        return null;
    }

    // 쿠키에 저장된 refresh token을 추출하는 메서드
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("cookie: {}", cookie.getName());
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // refresh token을 쿠키에 저장하는 메서드
    public void setRefreshTokenAtCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie", "SameSite=None");
    }

    // Access Token을 발급하는 메서드
    public String createAccessToken(String email) {
        log.info("Creating access token for email: " + email);
        Date nowTime = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(nowTime)  // 현재 시간을 Date로 설정
                .setExpiration(new Date(nowTime.getTime() + accessTokenExpirationInMs))  // 만료일 계산
                .claim("role", "USER")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Refresh Token을 발급하는 메서드
    public String createRefreshToken(String email) {
        log.info("Creating refresh token for email: " + email);
        Date nowTime = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(nowTime)  // 현재 시간을 Date로 설정
                .setExpiration(new Date(nowTime.getTime() + refreshTokenExpirationInMs))  // 만료일 계산
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // 토큰을 파싱하여 Claims 객체를 반환하는 메서드
    public Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    // refresh token이 유효한 지 검사하는 메서드
    public boolean validateToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            return !isTokenExpired(refreshToken);  // 만료되지 않았으면 true 반환
        } catch (Exception e) {
            return false;  // 파싱 실패나 만료된 경우 false 반환
        }
    }

    // refresh token 쿠키에서 삭제하는 메서드
    public void deleteRefreshTokenFromCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // 토큰에서 사용자의 이메일을 추출하는 메서드
    public String getEmailFromToken(String token) {
        try {
            return parseClaims(token).getSubject(); // 만료되지 않은 토큰에서 이메일 추출
        } catch (ExpiredJwtException e) {
            return ""; // 만약 토큰이 만료되면 빈 문자열 반환
        }
    }

    // 토큰이 만료되었는지 확인하는 메서드
    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);  // JWT 토큰에서 만료일을 Date로 추출
        return expirationDate.before(new Date());  // 현재 시간과 비교
    }

    // 토큰의 만료 날짜를 추출하는 메서드
    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();  // JWT에서 만료일을 Date로 추출
    }
}
