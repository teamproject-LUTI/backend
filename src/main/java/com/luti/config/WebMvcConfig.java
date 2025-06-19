package com.luti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String profileUploadDir;

    @Value("${file.upload.general.dir}")
    private String generalUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1. 프로필 이미지용
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + profileUploadDir + "/")
                .setCachePeriod(3600) // 1시간 캐시
                .resourceChain(true);

        // 2. 일반 파일용
        String generalUploadPath = Paths.get(generalUploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(generalUploadPath);

        // 로그로 설정 확인
        System.out.println("정적 리소스 설정 완료:");
        System.out.println("- 프로필 이미지: /files/** -> " + profileUploadDir);
        System.out.println("- 일반 파일: /uploads/** -> " + generalUploadPath);
    }

}
