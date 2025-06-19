package com.luti.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /** 업로드된 파일의 실제 경로 */
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

        /** "/uploads/**" 요청은 로컬 "uploads/" 폴더에서 찾도록 매핑 */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

}
