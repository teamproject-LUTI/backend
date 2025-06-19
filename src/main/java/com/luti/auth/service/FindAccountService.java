package com.luti.auth.service;

import com.luti.auth.dto.FindEmailRequestDto;
import com.luti.auth.dto.FindEmailResponseDto;
import com.luti.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FindAccountService {

    private final UserRepository userRepository;

    public Optional<FindEmailResponseDto> findMaskedEmail(FindEmailRequestDto requestDto) {
        return userRepository.findByNameAndPhoneNumber(requestDto.getName(), requestDto.getPhoneNumber())
                .map(user -> new FindEmailResponseDto(maskedEmail(user.getEmail())));
    }

    private String maskedEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email; // 너무 짧으면 그대로 반환
        return email.substring(0, 2) + "*".repeat(atIndex - 2) + email.substring(atIndex);
    }
}
