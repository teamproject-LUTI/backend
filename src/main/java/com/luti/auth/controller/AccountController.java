package com.luti.auth.controller;

import com.luti.auth.dto.*;
import com.luti.auth.service.EmailService;
import com.luti.auth.service.FindAccountService;
import com.luti.auth.service.SignupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final FindAccountService findAccountService;
    private final EmailService emailService;
    private final SignupService signupService;


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

    @PostMapping("/email/code")
    public ResponseEntity<String> sendEmail(@RequestBody EmailVerificationRequestDto requestDto, HttpServletRequest request) {
        try {
            String code = emailService.sendEmail(requestDto); // 6자리 코드 전송
            emailService.saveVerificationSession(request, code, requestDto.getEmail());
            return ResponseEntity.ok("인증코드가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/email/verify")
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

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        signupService.register(signupRequestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}

