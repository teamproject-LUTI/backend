package com.luti.auth.controller;

import com.luti.auth.dto.EmailRequestDto;
import com.luti.auth.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SignupController {

    @Autowired
    private final EmailService emailService;

    // 1. 이메일 인증코드 전송
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDto requestDto, HttpServletRequest request) {
        emailService.checkDuplicate(requestDto);
        try {
            String code = emailService.sendEmail(requestDto); // 6자리 코드 전송

            HttpSession session = request.getSession();
            session.setAttribute("authCode", code);
            session.setAttribute("authEmail", requestDto.getEmail());
            session.setMaxInactiveInterval(300); // 5분

            return ResponseEntity.ok("인증코드가 이메일로 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam("code") String code, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // 세션이 없거나 인증 시간 만료
        if (session == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 시간이 만료되었습니다.");
        }

        String savedCode = (String) session.getAttribute("authCode");

        // 인증 코드 일치 확인
        if (!savedCode.equals(code)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증번호가 일치하지 않습니다.");
        }

        // 인증 완료 시 세션에서 제거
        session.removeAttribute("authCode");
        session.removeAttribute("authEmail");

        return ResponseEntity.ok("이메일 인증 완료");
    }
}
