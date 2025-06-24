package com.luti.auth.service;

import com.luti.auth.dto.FindEmailRequestDto;
import com.luti.auth.dto.FindEmailResponseDto;
import com.luti.auth.dto.FindPasswordRequestDto;
import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FindAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public Optional<FindEmailResponseDto> findMaskedEmail(FindEmailRequestDto requestDto) {
        return userRepository.findByNameAndPhoneNumber(requestDto.getName(), requestDto.getPhoneNumber())
                .map(user -> new FindEmailResponseDto(maskedEmail(user.getEmail())));
    }

    public void resetAndSendTempPassword(FindPasswordRequestDto requestDto) throws MessagingException, UnsupportedEncodingException {
        User user = userRepository.findByNameAndEmail(requestDto.getName(), requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));

        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        emailService.sendTempPassword(requestDto.getEmail(), tempPassword);
    }

    private String maskedEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email; // 너무 짧으면 그대로 반환
        return email.substring(0, 2) + "*".repeat(atIndex - 2) + email.substring(atIndex);
    }

    private String generateTempPassword() {
        int length = 10;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
}
