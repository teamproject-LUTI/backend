package com.luti.auth.service;

import com.luti.auth.dto.SignupRequestDto;
import com.luti.auth.entity.User;
import com.luti.auth.entity.UserType;
import com.luti.auth.repository.UserRepository;
import com.luti.auth.repository.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(SignupRequestDto requestDto) {
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        UserType defaultUserType = userTypeRepository.findDefaultUserType()
                .orElseThrow(() -> new IllegalStateException("기본 유저 타입이 존재하지 않습니다."));

        User user = User.createRegularUser(requestDto, encodedPassword, defaultUserType);
        userRepository.save(user);
    }
}
