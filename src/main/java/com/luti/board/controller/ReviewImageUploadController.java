package com.luti.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/** TOAST UI 에디터용 이미지 업로드 컨트롤러 */
@RestController
@RequestMapping("/api/review-attachments")
public class ReviewImageUploadController {

    @PostMapping("/image")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadImage(@RequestPart MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("이미지가 비어있어요!");
        }

        // 프로젝트 루트 경로에 uploads 폴더 생성
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        String ext = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf('.') + 1);
        String uuid = UUID.randomUUID().toString();
        String storedName = uuid + "." + ext;

        File dest = new File(uploadDir, storedName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            image.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("업로드 실패", e);
        }

        return Map.of("url", "/uploads/" + storedName);
    }
}
