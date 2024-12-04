package com.sk.backend.controller;

import com.sk.backend.config.JwtTokenProvider;
import com.sk.backend.entity.Member;
import com.sk.backend.service.MailService;
import com.sk.backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "Authorization", allowCredentials = "true")
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final MailService mailService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Member member) {
        try {
            memberService.register(member);
            return ResponseEntity.status(201).body("Success");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody Map<String, String> data) {
        try {
            mailService.send(data);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody Map<String, Object> data) {
        try {
            boolean flag = mailService.verify(data);
            return flag ? ResponseEntity.ok("Success") : ResponseEntity.status(400).body("Error");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> data, HttpServletResponse response) {
        try {
            memberService.login(data, response);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("error" + e.getMessage());
        }
    }

    @GetMapping("/check/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable("nickname") String nickname) {
        log.info("Checking nickname {}", nickname);
        try {
            boolean isPossible = memberService.checkNinckname(nickname);
            return ResponseEntity.ok(isPossible);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refreshing data");
        String refreshToken = jwtTokenProvider.getRefreshTokenFromCookie(request);
        log.info("Received cookies: {}", Arrays.toString(request.getCookies()));
        log.info("Refresh token {}", refreshToken);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        jwtTokenProvider.deleteRefreshTokenFromCookie(response);
        return ResponseEntity.ok(newAccessToken);
    }
}
