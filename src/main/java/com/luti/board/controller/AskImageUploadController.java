package com.luti.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * TOAST UI 에디터용 문의(Ask) 첨부 이미지 업로드 컨트롤러
 */
@RestController
@RequestMapping("/api/ask-attachments")
public class AskImageUploadController {

    @PostMapping("/image")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadImage(@RequestPart("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("이미지가 비어있어요!");
        }

        // 프로젝트 루트 경로에 uploads/asks 폴더 생성
        String uploadDir = System.getProperty("user.dir") + "/uploads/asks";
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // 원본 확장자 추출
        String original = image.getOriginalFilename();
        String ext = original.substring(original.lastIndexOf('.') + 1);

        // UUID 파일명
        String uuid = UUID.randomUUID().toString();
        String storedName = uuid + "." + ext;

        File dest = new File(uploadPath, storedName);
        try {
            image.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("이미지 업로드 실패", e);
        }

        // 프론트에서 접근할 URL (WebMvcConfig 에서 /uploads/** 매핑 필요)
        String url = "/uploads/asks/" + storedName;
        return Map.of("url", url);
    }
}
