package com.sk.backend.service;

import com.sk.backend.config.JwtTokenProvider;
import com.sk.backend.entity.RefreshToken;
import com.sk.backend.repository.MemberRepository;
import com.sk.backend.entity.Member;
import com.sk.backend.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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

    public void login(Map<String, String> data, HttpServletResponse response) {
        String email = data.get("email");
        String password = data.get("password");
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        response.setHeader("Authorization", "Bearer " + accessToken);
        saveRefreshToken(email, refreshToken);
        jwtTokenProvider.setRefreshTokenAtCookie(refreshToken, response);

    }

    private void saveRefreshToken(String email, String refreshToken) {
        // RefreshToken 조회
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByEmail(email);

        // 이미 RefreshToken이 존재하면 업데이트, 없으면 새로 저장
        RefreshToken token = existingToken.orElse(new RefreshToken());
        token.setEmail(email);
        token.setToken(refreshToken);
        token.setCreatedDate(new Date()); // 생성일을 Date로 설정
        token.setExpirationDate(jwtTokenProvider.getExpirationDateFromToken(refreshToken)); // 만료일을 Date로 설정

        // DB에 저장
        refreshTokenRepository.save(token);
    }


    public boolean checkNinckname(String nickname) {
        boolean isPossible = memberRepository.checkNickname(nickname);
        log.info("checkNinckname: " + isPossible);
        return memberRepository.checkNickname(nickname);
    }

    public String getNicknameByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member != null) {
            return member.getNickname();
        } else {
            return ""; // Member가 존재하지 않으면 빈 문자열 반환
        }
    }
}
