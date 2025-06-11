package com.luti.auth.dto;

import com.luti.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // UserType의 typeName을 권한으로 사용
        return Collections.singleton(() -> user.getUserType().getTypeName());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (기본 true)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (기본 true)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 (기본 true)
    }

    @Override
    public boolean isEnabled() {
        return !"Y".equalsIgnoreCase(user.getWithdrawYn()); // 탈퇴 여부에 따라 설정
    }
}
