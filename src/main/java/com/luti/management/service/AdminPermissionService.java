package com.luti.management.service;

import com.luti.auth.repository.UserRepository;
import com.luti.auth.enums.UserTypeEnum;
import com.luti.auth.security.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminPermissionService {

    private final UserRepository userRepository;

    /**
     * 현재 사용자가 관리자인지 확인합니다.
     *
     * @return 관리자이면 true, 아니면 false
     */
    public boolean isCurrentUserAdmin() {
        Integer userTypeId = getCurrentUserTypeId();
        // UserTypeEnum.ADMIN.getId()가 Long 타입일 수 있으므로 equals로 비교
        return UserTypeEnum.ADMIN.getId().equals(userTypeId.longValue());
    }

    /**
     * 현재 인증된 사용자의 UserType ID를 DB에서 조회하여 반환합니다.
     *
     * @return 사용자의 UserType ID
     * @throws UsernameNotFoundException 사용자를 DB에서 찾을 수 없는 경우
     */
    public Integer getCurrentUserTypeId() {
        Long currentUserId = getCurrentUserId();

        log.debug("DB에서 사용자 ID {}의 userTypeId 조회 시도", currentUserId);

        return Optional.ofNullable(userRepository.findByUserId(currentUserId))
                .map(user -> {
                    var userType = user.getUserTypeId(); // UserType 엔티티를 가져옵니다.
                    if (userType == null) {
                        // 사용자 유형이 없는 경우에 대한 예외 처리
                        log.error("사용자 ID {}의 사용자 유형(userType)이 null입니다. 데이터 무결성을 확인해야 합니다.", currentUserId);
                        throw new IllegalStateException("사용자 ID " + currentUserId + "에 대한 사용자 유형 정보가 없습니다.");
                    }
                    // UserType에서 실제 ID 값을 가져옵니다.
                    return userType.getUserTypeId().intValue(); // 데이터 손실 가능성에 유의
                })
                .orElseThrow(() -> {
                    // ofNullable로 감싼 Optional이 비어있을 때 (사용자를 못 찾았을 때) 실행됩니다.
                    log.error("보안 컨텍스트에 있는 사용자 ID {}를 DB에서 찾을 수 없습니다.", currentUserId);
                    return new UsernameNotFoundException("사용자 ID " + currentUserId + "를 찾을 수 없습니다.");
                });

    }

    /**
     * SecurityContext에서 현재 로그인된 사용자의 ID를 가져옵니다.
     * 모든 요청은 인증되었으므로, 토큰에서 ID를 직접 추출합니다.
     *
     * @return 사용자 ID
     * @throws AuthenticationCredentialsNotFoundException 인증 정보가 없거나 ID가 없는 경우
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Long userId = jwtAuth.getCurrentUserId();
            if (userId != null) {
                return userId;
            }
        }

        // 이 로직이 실행된다는 것은 인증 시스템에 문제가 있다는 의미
        log.error("인증 토큰(JwtAuthenticationToken) 또는 사용자 ID를 찾을 수 없습니다.");
        throw new AuthenticationCredentialsNotFoundException("인증 정보에서 사용자 ID를 찾을 수 없습니다.");
    }

    // 이 메서드는 더 이상 필요하지 않을 수 있지만, 명시적인 권한 확인이 필요할 경우를 위해 남겨둡니다.
    public void requireAdminPermission(String operation) {
        if (!isCurrentUserAdmin()) {
            log.warn("권한 없는 접근 시도 - 사용자 ID: {}, 작업: {}", getCurrentUserId(), operation);
            throw new SecurityException("관리자 권한이 필요합니다: " + operation); // 혹은 AccessDeniedException
        }
    }
}