package com.luti.auth.service;

import com.luti.auth.dto.CustomUserDetails;
import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 사용자 정보를 데이터베이스에서 로드하고, 이를 CustomUserDetails로 변환하여 반환하는 역할
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId);

        if (user == null) {
            throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다: " + loginId);
        }

        return new CustomUserDetails(user);
    }
}
