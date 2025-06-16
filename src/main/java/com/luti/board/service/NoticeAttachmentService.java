package com.luti.board.service;

import com.luti.board.dto.NoticeAttachmentRequestDto;
import com.luti.board.dto.NoticeAttachmentResponseDto;
import com.luti.board.entity.Notice;
import com.luti.board.entity.NoticeAttachment;
import com.luti.board.repository.NoticeAttachmentRepository;
import com.luti.board.repository.NoticeRepository;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeAttachmentService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository;

    /**
     * 특정 공지사항의 첨부파일 목록을 조회합니다.
     *
     * @param noticeId 공지사항 고유번호
     */
    public MultiResponseDto<NoticeAttachmentResponseDto> getAttachments(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        List<NoticeAttachmentResponseDto> dtos = attachmentRepository
                .findAllByNotice(notice)
                .stream()
                .map(NoticeAttachmentResponseDto::of)
                .collect(Collectors.toList());

        // 페이지네이션 없이 전체 반환
        return new MultiResponseDto<>(dtos, null);
    }

    /**
     * 공지사항에 첨부파일을 추가합니다.
     *
     * @param noticeId 공지사항 고유번호
     * @param dto      첨부파일 메타데이터 DTO
     */
    @Transactional
    public SingleResponseDto<NoticeAttachmentResponseDto> addAttachment(
            Long noticeId,
            NoticeAttachmentRequestDto dto) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found: " + noticeId));

        // 엔티티 생성 및 양방향 연관관계 설정
        NoticeAttachment attachment = NoticeAttachment.builder()
                .fileName(dto.getFileName())
                .physicalPath(dto.getPhysicalPath())
                .logicalPath(dto.getLogicalPath())
                .extension(dto.getExtension())
                .size(dto.getSize())
                .build();
        attachment.linkToNotice(notice);

        NoticeAttachment saved = attachmentRepository.save(attachment);

        return new SingleResponseDto<>(NoticeAttachmentResponseDto.of(saved));
    }

    /**
     * 첨부파일을 삭제합니다. (완전 삭제)
     *
     * @param fileNo 삭제할 첨부파일 고유번호
     */
    @Transactional
    public void deleteAttachment(Long fileNo) {
        NoticeAttachment attachment = attachmentRepository.findById(fileNo)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + fileNo));

        attachment.unlinkFromNotice();
        attachmentRepository.delete(attachment);
    }
}
