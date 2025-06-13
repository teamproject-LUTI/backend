package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.NoticeRequestDto;
import com.luti.board.dto.NoticeResponseDto;
import com.luti.board.entity.Notice;
import com.luti.board.repository.NoticeRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    /**
     * 새 공지사항을 등록
     *
     * @param userId 작성자 User의 PK
     * @param dto    제목(title)과 본문(content)을 담은 요청 DTO
     * @return 생성된 공지사항을 래핑한 SingleResponseDto
     */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> createNotice(Long userId, NoticeRequestDto dto) {
        // 1) 사용자 존재 여부 확인
        if (!userRepository.existsByUserId(userId)) {
            throw new EntityNotFoundException("User not found: " + userId);
        }
        // 2) User 엔티티 조회
        User user = userRepository.findByUserId(userId);

        // 3) Notice 엔티티 생성 및 저장
        Notice notice = Notice.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        Notice saved = noticeRepository.save(notice);

        // 4) 응답 DTO로 변환 후 반환
        return new SingleResponseDto<>(NoticeResponseDto.of(saved));
    }

    /**
     * 공지사항 목록을 페이지 단위로 조회
     *
     * @param page 요청 페이지 번호 (1-based)
     * @param size 페이지당 항목 수
     * @return 페이징된 공지사항 목록을 담은 MultiResponseDto
     */
    public MultiResponseDto<NoticeResponseDto> getNotices(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<NoticeResponseDto> dtoPage = noticeRepository
                .findAll(pageRequest)
                .map(NoticeResponseDto::of);

        return new MultiResponseDto<>(dtoPage.getContent(), dtoPage);
    }

    /**
     * 특정 공지사항을 조회
     *
     * @param noticeNo 공지사항 고유번호
     * @return 조회된 공지사항을 래핑한 SingleResponseDto
     */
    public SingleResponseDto<NoticeResponseDto> getNotice(Long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeNo));

        return new SingleResponseDto<>(NoticeResponseDto.of(notice));
    }

    /**
     * 공지사항을 수정
     *
     * @param noticeNo 수정할 공지사항 고유번호
     * @param dto      변경할 제목과 본문을 담은 요청 DTO
     * @return 수정된 공지사항을 래핑한 SingleResponseDto
     */
    @Transactional
    public SingleResponseDto<NoticeResponseDto> updateNotice(Long noticeNo, NoticeRequestDto dto) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeNo));

        // 변경 가능한 필드 업데이트 (Dirty Checking)
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        return new SingleResponseDto<>(NoticeResponseDto.of(notice));
    }

    /**
     * 공지사항을 soft-delete 처리
     *
     * @param noticeNo 삭제(soft) 처리할 공지사항 고유번호
     */
    @Transactional
    public void deleteNotice(Long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeNo));

        // deleted 플래그를 true 로 설정하여 soft delete
        notice.markDeleted();
    }
}
