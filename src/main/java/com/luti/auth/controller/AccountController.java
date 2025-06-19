package com.luti.auth.controller;

import com.luti.auth.dto.FindEmailRequestDto;
import com.luti.auth.dto.FindEmailResponseDto;
import com.luti.auth.service.FindAccountService;
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

    @PostMapping("/find-email")
    public ResponseEntity<Map<String, String>> findUserEmail(@RequestBody FindEmailRequestDto requestDto) {
        Optional<FindEmailResponseDto> responseDto = findAccountService.findMaskedEmail(requestDto);

        if (responseDto.isPresent()) {
            return ResponseEntity.ok(Map.of("email", responseDto.get().getMaskedEmail()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "일치하는 회원정보가 없습니다."));
        }
    }
}

