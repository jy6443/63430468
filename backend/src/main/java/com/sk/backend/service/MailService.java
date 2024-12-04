package com.sk.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailService {
    @Value("${spring.mail.username}") private String sender;
    private final JavaMailSender javaMailSender;
    private final Map<String, Integer> verificationCode = new HashMap<>();
    private static int createCode() {
        return (int) (Math.random() * (90000)) + 100000; // 인증 코드로 6자리의 랜덤 수를 생성합니다.
    }

    public MimeMessage createMessage(String email) {
        int code = createCode();
        verificationCode.put(email, code);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            mimeMessage.setFrom(sender);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, email);
            mimeMessage.setSubject("이메일 인증");
            String body = String.format(
                    "<h3>요청하신 인증 번호입니다.</h3><h1>%d</h1><h3>감사합니다.</h3>",
                    code
            );
            mimeMessage.setText(body, "UTF-8", "html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mimeMessage;
    }

    public void send(Map<String, String> data) {
        String email = data.get("email");
        MimeMessage mimeMessage = createMessage(email);
        javaMailSender.send(mimeMessage);
    }

    public boolean verify(Map<String, Object> data) {
        String email = (String) data.get("email");
        int code = Integer.parseInt((String) data.get("code"));
        Integer storedCode = verificationCode.get(email);
        return storedCode != null && storedCode == code;
    }
}
