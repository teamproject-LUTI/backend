package com.luti.board.controller;

import com.luti.board.dto.ReviewAttachmentRequestDto;
import com.luti.board.dto.ReviewAttachmentResponseDto;
import com.luti.board.service.ReviewAttachmentService;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/reviews/{reviewId}/attachments")
@RequiredArgsConstructor
public class ReviewAttachmentController {

    private final ReviewAttachmentService service;

    /** 1) 특정 리뷰의 첨부파일 목록 조회 */
    @GetMapping
    public MultiResponseDto<ReviewAttachmentResponseDto> getAttachments(
            @PathVariable Long reviewId) {
        return service.getAttachments(reviewId);
    }

    /** 2) 리뷰에 첨부파일 추가 (Multipart) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SingleResponseDto<ReviewAttachmentResponseDto> addAttachments(
            @PathVariable Long reviewId,
            @RequestParam ("files") List<MultipartFile> files) {
        // 서비스 계층에서 MultipartFile 리스트 처리하도록 구현
        return service.addAttachmentFiles(reviewId, files);
    }

    /** 4) 첨부파일 다운로드 (PDF, Excel 등) */
    @GetMapping("/{fileNo}/download")
    public ResponseEntity<Resource> download(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable Long fileNo) throws IOException {

        // 1) Service에서 단일 DTO 가져오기
        ReviewAttachmentResponseDto dto = service.getAttachmentDto(fileNo);

        // 2) 물리경로로 Resource 생성
        Path path = Paths.get(dto.getPhysicalPath());
        Resource resource = new UrlResource(path.toUri());

        // 3) 브라우저에서 바로 열리게 inline, 파일명 인코딩
        String encoded = URLEncoder.encode(dto.getFileName(), StandardCharsets.UTF_8);
        String contentType = Files.probeContentType(path);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"")
                .body(resource);
    }


    /** 3) 첨부파일 삭제  */
    @DeleteMapping("/{fileNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(
            @PathVariable Long reviewId,
            @PathVariable Long fileNo) {
        service.deleteAttachment(fileNo);
    }



}
