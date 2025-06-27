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
import org.springframework.data.jpa.domain.Specification;
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
    private void ensureAdmin() {
        adminPermissionService.requireAdminPermission("공지사항 관리");
    }

    /** 권한 체크 유틸리티 메서드 */
    private boolean hasPermissionToModify(Notice notice, Long userId) {
        // 관리자인지 확인
        return userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());
    }

    /** 공지 등록 (관리자만) */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> createNotice(Long userId, NoticeRequestDto dto) {
        ensureAdmin();

        Long currentUserId = adminPermissionService.getCurrentUserId();
        User user = userRepository.findByUserIdWithUserType(currentUserId);

        Notice notice = Notice.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        Notice saved = noticeRepository.save(notice);

        return new SingleResponseDto<>(NoticeResponseDto.of(saved, currentUserId));
    }

    /** 목록 조회 (누구나 가능 + 검색) - 삭제되지 않은 글만 조회 */
    public MultiResponseDto<NoticeResponseDto> getNotices(int page, int size, Long currentUserId, String searchType, String keyword) {
        // 1) Specification 초기화 : 삭제되지 않은 글만 조회
        Specification<Notice> spec = Specification.where((root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("deleted")),
                        cb.equal(root.get("deleted"), false)
                )
        );

        // 2) 검색어(keyword)가 존재하면 검색조건(searchType)에 따라 동적 조건 추가
        if (keyword != null && !keyword.isBlank()) {
            switch (searchType) {
                case "author":
                    spec = spec.and((root, query, cb) ->
                            cb.like(root.get("user").get("nickname"), "%" + keyword + "%"));
                    break;
                case "title":
                    spec = spec.and((root, query, cb) ->
                            cb.like(root.get("title"), "%" + keyword + "%"));
                    break;
                case "content":
                    spec = spec.and((root, query, cb) ->
                            cb.like(root.get("content"), "%" + keyword + "%"));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown searchType: " + searchType);
            }
        }

        //페이징, 정렬 : 최신순(내림차순)
        //spec과 페이징,정렬 정보를 한번에 넘겨서 조회
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notice> notices = noticeRepository.findAll(spec, pageRequest);

        //엔티티 -> DTO변환 (관리자 여부 포함)
        boolean isAdmin = currentUserId != null && userRepository.isUserAdmin(currentUserId, UserTypeEnum.ADMIN.getId());
        Page<NoticeResponseDto> dtoPage = notices.map(n -> NoticeResponseDto.of(n, currentUserId, isAdmin));

        return new MultiResponseDto<>(dtoPage.getContent(), dtoPage);
    }

    /** 단일 조회 (누구나 가능) - 삭제된 글은 조회 불가 */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> getNotice(Long noticeId, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 삭제된 글인지 확인
        if (Boolean.TRUE.equals(notice.getDeleted())) {
            throw new EntityNotFoundException("Notice has been deleted: " + noticeId);
        }

        // 조회수 증가
        notice.increaseViewCount();

        // 관리자 여부 확인
        boolean isAdmin = userId != null && userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

        return new SingleResponseDto<>(NoticeResponseDto.of(notice, userId, isAdmin));
    }

    /** 수정 (관리자만) */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> updateNotice(
            Long noticeId, NoticeRequestDto dto, Long userId) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 삭제된 글인지 확인
        if (Boolean.TRUE.equals(notice.getDeleted())) {
            throw new EntityNotFoundException("Notice has been deleted: " + noticeId);
        }

        // 관리자 권한 확인
        if (!hasPermissionToModify(notice, userId)) {
            throw new SecurityException("Access denied: Only admin can modify notices");
        }

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        // 관리자 여부 확인
        boolean isAdmin = userId != null && userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

        return new SingleResponseDto<>(NoticeResponseDto.of(notice, userId, isAdmin));
    }

    /** 삭제 (관리자만, soft-delete) */
    @Transactional
    public void deleteNotice(Long noticeId, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 이미 삭제된 글인지 확인
        if (Boolean.TRUE.equals(notice.getDeleted())) {
            throw new EntityNotFoundException("Notice has already been deleted: " + noticeId);
        }

        // 관리자 권한 확인
        if (!hasPermissionToModify(notice, userId)) {
            throw new SecurityException("Access denied: Only admin can delete notices");
        }

        notice.markDeleted();
    }
}