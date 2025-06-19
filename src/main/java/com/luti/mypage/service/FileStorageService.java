package com.luti.mypage.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 저장 및 관리를 담당하는 서비스
 */
@Slf4j
@Service
public class FileStorageService {

	@Value("${file.upload.dir:uploads}")
	private String uploadDir;

	@Value("${file.upload.profile.dir:profiles}")
	private String profileDir;

	@Value("${server.domain:http://localhost:8080}")
	private String serverDomain;

	// 허용되는 이미지 파일 확장자
	private static final List<String> ALLOWED_IMAGE_EXTENSIONS =
			Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

	/**
	 * 프로필 이미지 파일 저장
	 *
	 * @param file 업로드할 파일
	 * @param userId 사용자 ID
	 * @return ProfileImageInfo 저장된 파일 정보
	 * @throws IOException 파일 저장 중 오류 발생 시
	 */
	public ProfileImageInfo saveProfileImage(MultipartFile file, Long userId) throws IOException {
		log.info("프로필 이미지 저장 시작 - 사용자 ID: {}, 파일명: {}", userId, file.getOriginalFilename());

		// 파일 유효성 검증
		validateImageFile(file);

		// 저장 경로 생성 (년/월/일 구조)
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String profilePath = profileDir + "/" + datePath;

		// 절대 경로로 업로드 디렉토리 설정
		Path uploadPath = Paths.get(uploadDir).toAbsolutePath().resolve(profilePath);

		// 디렉토리 생성
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
			log.info("디렉토리 생성: {}", uploadPath);
		}

		// 고유한 파일명 생성
		String originalFilename = file.getOriginalFilename();
		String extension = getFileExtension(originalFilename);
		String uniqueFilename = generateUniqueFilename(userId, extension);

		// 파일 저장
		Path filePath = uploadPath.resolve(uniqueFilename);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		// 파일 정보 반환
		String physicalPath = filePath.toString();
		String logicalPath = "/files/" + profilePath + "/" + uniqueFilename;  // ⭐ /files/ 경로로 변경
		String accessUrl = serverDomain + logicalPath;

		log.info("프로필 이미지 저장 완료:");
		log.info("- 사용자 ID: {}", userId);
		log.info("- 파일명: {}", uniqueFilename);
		log.info("- 물리적 경로: {}", physicalPath);
		log.info("- 논리적 경로: {}", logicalPath);
		log.info("- 접근 URL: {}", accessUrl);

		return ProfileImageInfo.builder()
				.fileName(uniqueFilename)
				.physicalPath(physicalPath)
				.logicalPath(logicalPath)
				.accessUrl(accessUrl)
				.extension(extension)
				.size(file.getSize())
				.build();
	}

	/**
	 * 기존 프로필 이미지 파일 삭제
	 *
	 * @param physicalPath 삭제할 파일의 물리적 경로
	 */
	public void deleteProfileImage(String physicalPath) {
		if (physicalPath == null || physicalPath.trim().isEmpty()) {
			log.debug("삭제할 파일 경로가 없음");
			return;
		}

		try {
			Path filePath = Paths.get(physicalPath);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				log.info("기존 프로필 이미지 삭제 완료: {}", physicalPath);
			} else {
				log.warn("삭제하려는 파일이 존재하지 않음: {}", physicalPath);
			}
		} catch (IOException e) {
			log.error("프로필 이미지 삭제 실패: {}", physicalPath, e);
			// 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}

	/**
	 * 이미지 파일 유효성 검증
	 *
	 * @param file 검증할 파일
	 * @throws IllegalArgumentException 유효하지 않은 파일인 경우
	 */
	private void validateImageFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 없습니다.");
		}

		// 파일명 검증
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.trim().isEmpty()) {
			throw new IllegalArgumentException("유효하지 않은 파일명입니다.");
		}

		// 파일 확장자 검증
		String extension = getFileExtension(originalFilename);
		if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
		}

		// Content-Type 검증
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
		}

		log.debug("파일 유효성 검증 완료 - 파일명: {}, 크기: {}, 타입: {}",
				originalFilename, file.getSize(), contentType);
	}

	/**
	 * 파일 확장자 추출
	 *
	 * @param filename 파일명
	 * @return 확장자 (소문자)
	 * @throws IllegalArgumentException 유효하지 않은 파일명인 경우
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			throw new IllegalArgumentException("유효하지 않은 파일명입니다.");
		}
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}

	/**
	 * 고유한 파일명 생성
	 * 패턴: profile_{userId}_{timestamp}_{uuid}.{extension}
	 *
	 * @param userId 사용자 ID
	 * @param extension 파일 확장자
	 * @return 고유한 파일명
	 */
	private String generateUniqueFilename(Long userId, String extension) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		return String.format("profile_%d_%s_%s.%s", userId, timestamp, uuid, extension);
	}

	/**
	 * 프로필 이미지 정보를 담는 내부 클래스
	 */
	@lombok.Builder
	@lombok.Getter
	public static class ProfileImageInfo {

		private String fileName;        // 저장된 파일명

		private String physicalPath;    // 실제 파일 경로

		private String logicalPath;     // 웹 접근 경로

		private String accessUrl;       // 완전한 접근 URL

		private String extension;       // 파일 확장자

		private Long size;             // 파일 크기 (바이트)

	}

}
