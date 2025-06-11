package com.luti.auth.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsMvcConfig implements WebMvcConfigurer {

    //Spring MVC 요청 처리에 CORS를 적용하는 코드
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry.addMapping("/**") //모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000"); //허용할 Origin 명시

    }
}
