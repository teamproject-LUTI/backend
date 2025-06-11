package com.luti.auth.service;

import com.luti.auth.dto.JoinDto;
import com.luti.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JoinService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void checkDuplication(JoinDto joinDTO) {
        if (userRepository.existsByLoginId(joinDTO.getLoginId())) {
            throw new IllegalAccessException("이미 가입된 이메일입니다.");
        }


    }

    public void sendMail(JoinDto joinDTO) {

    }
}
