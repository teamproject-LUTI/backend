package com.luti.auth.controller;

import com.luti.auth.dto.EmailRequestDto;
import com.luti.auth.dto.FindEmailRequestDto;
import com.luti.auth.dto.FindEmailResponseDto;
import com.luti.auth.dto.FindPasswordRequestDto;
import com.luti.auth.service.EmailService;
import com.luti.auth.service.FindAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final FindAccountService findAccountService;
    private final EmailService emailService;

    @PostMapping("/find-email")
    public ResponseEntity<Map<String, String>> findUserEmail(@RequestBody FindEmailRequestDto requestDto) {
        Optional<FindEmailResponseDto> responseDto = findAccountService.findMaskedEmail(requestDto);

        if (responseDto.isPresent()) {
            return ResponseEntity.ok(Map.of("email", responseDto.get().getMaskedEmail()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "일치하는 회원정보가 없습니다."));
        }
    }

    @PostMapping("/find-password")
    public ResponseEntity<Map<String, String>> findUserPassword(@RequestBody FindPasswordRequestDto requestDto, HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("authCode") != null || session.getAttribute("authEmail") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "이메일 인증이 완료되지 않았습니다."));
        }

        String authEmail = (String) session.getAttribute("authEmail");

        if (!authEmail.equals(requestDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "인증된 이메일과 일치하지 않습니다."));
        }

        try {
            findAccountService.resetAndSendTempPassword(requestDto);
            session.invalidate(); // 인증 완료 후 세션 무효화
            return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 전송되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }
}

