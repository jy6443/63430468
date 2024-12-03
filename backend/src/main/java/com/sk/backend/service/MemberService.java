package com.sk.backend.service;

import com.sk.backend.config.JwtTokenProvider;
import com.sk.backend.repository.MemberRepository;
import com.sk.backend.entity.Member;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String pwPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
    // 알파벳 + 숫자 + 특수기호로 이루어진 8자리 이상의 비밀번호만 허락하는 제약조건입니다.

    public void register(Member member) {
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!isValidPassword(member.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);
    }

    public boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile(pwPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public Map<String, String> login(Map<String, String> data) {
        String email = data.get("email");
        String password = data.get("password");
        log.info("email: {}, password: {}", email, password);
        Map<String, String> map = new HashMap<>();
        Member member = memberRepository.findByEmail(email);
        log.info("member found: {}", member);
        if (member == null) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        log.info("access token: {}, refresh token: {}", accessToken, refreshToken);
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        return map;
    }

    public boolean checkNinckname(String nickname) {
        return memberRepository.checkNickname(nickname);
    }
}
