package com.sk.backend.controller;

import com.sk.backend.entity.Member;
import com.sk.backend.service.MailService;
import com.sk.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final MailService mailService;

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
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> data) {
        try {
            Map<String, String> tokens = memberService.login(data);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable String nickname) {
        try {
            boolean isPossible = memberService.checkNinckname(nickname);
            return ResponseEntity.ok(isPossible);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }
}
