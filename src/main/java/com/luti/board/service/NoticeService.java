package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.enums.UserTypeEnum;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.NoticeRequestDto;
import com.luti.board.dto.NoticeResponseDto;
import com.luti.board.entity.Notice;
import com.luti.management.service.AdminPermissionService;
import com.luti.board.repository.NoticeRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final AdminPermissionService adminPermissionService;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    /** 관리자 권한 체크 헬퍼 */
    /** 관리자 타입 ID 가져오기 - static 초기화 문제 해결 */
    private void ensureAdmin() {
        adminPermissionService.requireAdminPermission("공지사항 관리");
    }

    /** 공지 등록 (관리자만) */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> createNotice(Long userId, NoticeRequestDto dto) {
        ensureAdmin();

        Long currentUserId = adminPermissionService.getCurrentUserId();
        User user = userRepository.findByUserIdWithUserType(currentUserId);

//        User u = userRepository.findByUserId(userId);
//        if (u==null) throw new EntityNotFoundException();
        // 여기서 프록시 초기화 없이도 user.getUserTypeId().getUserTypeId() 호출로 숫자 비교 가능
//        if (!u.getUserTypeId().getUserTypeId().equals(ADMIN_TYPE_ID)) {
//            throw new SecurityException("관리자만 가능합니다.");
//        }

        Notice notice = Notice.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        Notice saved = noticeRepository.save(notice);

        return new SingleResponseDto<>(NoticeResponseDto.of(saved, currentUserId));
    }

    /** 목록 조회 (누구나 가능) */
    public MultiResponseDto<NoticeResponseDto> getNotices(int page, int size, Long currentUserId) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoticeResponseDto> dtoPage = noticeRepository
                .findAll(pageRequest)
                .map(n -> NoticeResponseDto.of(n, currentUserId));

        return new MultiResponseDto<>(dtoPage.getContent(), dtoPage);
    }

    /** 단일 조회 (누구나 가능) */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> getNotice(Long noticeId, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));
        // 조회수 증가
        notice.increaseViewCount();

        return new SingleResponseDto<>(NoticeResponseDto.of(notice, userId));
    }

    /** 수정 (관리자만) */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> updateNotice(
            Long noticeId, NoticeRequestDto dto, Long userId) {
//        ensureAdmin(userId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        return new SingleResponseDto<>(NoticeResponseDto.of(notice, userId));
    }

    /** 삭제 (관리자만, soft-delete) */
    @Transactional
    public void deleteNotice(Long noticeId, Long userId) {
//        ensureAdmin(userId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));
        notice.markDeleted();
    }
}
