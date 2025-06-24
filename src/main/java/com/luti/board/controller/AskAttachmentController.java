package com.luti.board.controller;

import com.luti.board.dto.AskAttachmentRequestDto;
import com.luti.board.dto.AskAttachmentResponseDto;
import com.luti.board.service.AskAttachmentService;
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

/**
 * {@code AskAttachmentController}는 문의글 첨부파일 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/asks/{askId}/attachments")
@RequiredArgsConstructor
public class AskAttachmentController {

    private final AskAttachmentService service;

    /** 첨부파일 목록 조회 */
    @GetMapping
    public MultiResponseDto<AskAttachmentResponseDto> list(
            @PathVariable Long askId) {
        List<AskAttachmentResponseDto> list = service.getAttachmentsByAsk(askId);
        return new MultiResponseDto<>(list, null);
    }

    /** 첨부파일 추가 (MultipartFile 방식) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<AskAttachmentResponseDto> addAttachmentFiles(
            @PathVariable Long askId,
            @RequestParam("files") List<MultipartFile> files) {
        return service.addAttachmentFiles(askId, files);
    }

    /** 첨부파일 등록 (메타데이터 방식 - 기존 유지) */
    @PostMapping("/metadata")
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<AskAttachmentResponseDto> create(
            @PathVariable Long askId,
            @RequestBody @Valid AskAttachmentRequestDto dto) {
        Long id = service.saveAttachment(askId, dto);
        AskAttachmentResponseDto resp = service.getAttachment(id);
        return new SingleResponseDto<>(resp);
    }

    /** 첨부파일 다운로드 */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long askId,
            @PathVariable Long attachmentId) throws IOException {

        // 1) Service에서 단일 DTO 가져오기
        AskAttachmentResponseDto dto = service.getAttachment(attachmentId);

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

    /** 특정 첨부파일 삭제 */
    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long attachmentId) {
        service.deleteAttachment(attachmentId);
    }
}