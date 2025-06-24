package com.luti.board.controller;

import com.luti.board.dto.NoticeAttachmentRequestDto;
import com.luti.board.dto.NoticeAttachmentResponseDto;
import com.luti.board.service.NoticeAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/notices/{noticeId}/attachments")
@RequiredArgsConstructor
public class NoticeAttachmentController {

    private final NoticeAttachmentService service;

    /**
     * 첨부파일 목록 조회
     */
    @GetMapping
    public MultiResponseDto<NoticeAttachmentResponseDto> list(
            @PathVariable Long noticeId) {
        return service.getAttachments(noticeId);
    }

    /**
     * 첨부파일 추가 (MultipartFile 방식)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<NoticeAttachmentResponseDto> addAttachmentFiles(
            @PathVariable Long noticeId,
            @RequestParam("files") List<MultipartFile> files) {
        return service.addAttachmentFiles(noticeId, files);
    }

    /**
     * 첨부파일 추가 (메타데이터 방식 - 기존 유지)
     */
    @PostMapping("/metadata")
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<NoticeAttachmentResponseDto> create(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeAttachmentRequestDto dto) {
        return service.addAttachment(noticeId, dto);
    }

    /**
     * 첨부파일 다운로드
     */
    @GetMapping("/{fileNo}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long noticeId,
            @PathVariable Long fileNo) throws IOException {

        // 1) Service에서 단일 DTO 가져오기
        NoticeAttachmentResponseDto dto = service.getAttachmentDto(fileNo);

        // 2) 물리경로로 Resource 생성
        Path path = Paths.get(dto.getPhysicalPath());
        Resource resource = new UrlResource(path.toUri());

        // 3) 브라우저에서 다운로드, 파일명 인코딩
        String encoded = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"")
                .body(resource);
    }

    /**
     * 첨부파일 삭제
     */
    @DeleteMapping("/{fileNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long noticeId,
            @PathVariable Long fileNo) {
        service.deleteAttachment(fileNo);
    }
}