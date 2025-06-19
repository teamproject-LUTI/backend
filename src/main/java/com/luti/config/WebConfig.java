package com.luti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 설정 - 정적 리소스 경로 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${file.upload.dir:uploads}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 업로드된 파일들을 정적 리소스로 서빙
		registry.addResourceHandler("/files/**")
				.addResourceLocations("file:" + uploadDir + "/")
				.setCachePeriod(3600) // 1시간 캐시
				.resourceChain(true);

		// 로그로 설정 확인
		System.out.println("정적 리소스 설정 완료:");
		System.out.println("- URL 패턴: /files/**");
		System.out.println("- 실제 경로: file:" + uploadDir + "/");
	}
}
