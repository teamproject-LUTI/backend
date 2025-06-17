package com.luti.board.service;

import com.luti.board.dto.AskAttachmentRequestDto;
import com.luti.board.dto.AskAttachmentResponseDto;
import com.luti.board.entity.Ask;
import com.luti.board.entity.AskAttachment;
import com.luti.board.repository.AskAttachmentRepository;
import com.luti.board.repository.AskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문의 첨부파일 기능을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskAttachmentService {

    private final AskAttachmentRepository attachmentRepository;
    private final AskRepository askRepository;

    /**
     * 첨부파일 등록
     */
    @Transactional
    public Long saveAttachment(Long askId, AskAttachmentRequestDto req) {
        Ask ask = askRepository.findById(askId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다. askId=" + askId));

        AskAttachment attachment = AskAttachment.builder()
                .fileName(req.getFileName())
                .physicalPath(req.getPhysicalPath())
                .logicalPath(req.getLogicalPath())
                .extension(req.getExtension())
                .size(req.getSize())
                .build();
        attachment.linkToAsk(ask);

        attachmentRepository.save(attachment);
        return attachment.getAskAttachmentId();
    }

    /**
     * 문의글에 딸린 첨부파일 전체 조회
     */
    public List<AskAttachmentResponseDto> getAttachmentsByAsk(Long askId) {
        return attachmentRepository.findByAskAskId(askId).stream()
                .map(AskAttachmentResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 단일 첨부파일 조회
     */
    public AskAttachmentResponseDto getAttachment(Long id) {
        AskAttachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + id));
        return AskAttachmentResponseDto.of(a);
    }

    /**
     * 첨부파일 삭제
     */
    @Transactional
    public void deleteAttachment(Long id) {
        AskAttachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다. id=" + id));
        attachmentRepository.delete(a);
    }
}
