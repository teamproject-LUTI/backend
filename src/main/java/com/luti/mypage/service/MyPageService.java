package com.luti.mypage.service;

import com.luti.auth.entity.User;
import com.luti.auth.repository.UserRepository;
import com.luti.mypage.dto.MyPageProfileResponseDto;
import com.luti.mypage.dto.request.MyPageProfileUpdateRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 마이페이지 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final UserRepository userRepository;

	/**
	 * 마이페이지 프로필 정보 조회
	 *
	 * @param userId 조회할 사용자 ID
	 * @return MyPageProfileResponseDto 마이페이지 프로필 정보
	 * @throws RuntimeException 사용자를 찾을 수 없는 경우
	 */
	public MyPageProfileResponseDto getMyPageProfile(Long userId) {
		log.info("마이페이지 프로필 조회 - 사용자 ID: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		return buildMyPageProfileResponse(user);
	}

	/**
	 * 마이페이지 프로필 정보 수정
	 * 모든 사용자에 대해 닉네임, 생년월일, 성별, 전화번호, 이메일, 주소 수정 가능하도록 통합 로직
	 *
	 * @param userId 수정할 사용자 ID
	 * @param requestDto 수정할 프로필 정보가 담긴 DTO
	 * @throws RuntimeException 사용자를 찾을 수 없는 경우 또는 기타 유효성 검사 실패 시
	 */
	@Transactional
	public void updateMyPageProfile(Long userId, MyPageProfileUpdateRequestDto requestDto) {
		log.info("마이페이지 프로필 수정 처리 - 사용자 ID: {}, 요청 DTO: {}", userId, requestDto);

		// 1. 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

		// 2. 모든 사용자(일반/소셜 무관)에 대해 공통적으로 정보 업데이트
		// 닉네임
		if (StringUtils.hasText(requestDto.getNickname())) {
			// 닉네임 중복 검사 (옵션)
			if (userRepository.existsByNickname(requestDto.getNickname()) && !user.getNickname().equals(requestDto.getNickname())) {
				throw new RuntimeException("이미 사용 중인 닉네임입니다.");
			}
			user.setNickname(requestDto.getNickname());
		}

		// 생년월일
		if (StringUtils.hasText(requestDto.getBirthday())) {
			try {
				// YYYY-MM-DD 형식 유효성 검사 및 저장
				LocalDate.parse(requestDto.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				user.setBirthday(requestDto.getBirthday());
			} catch (DateTimeParseException e) {
				throw new RuntimeException("잘못된 생년월일 형식입니다. (YYYY-MM-DD)");
			}
		}

		// 성별
		if (StringUtils.hasText(requestDto.getGender())) {
			String formattedGender = formatGenderForUpdate(requestDto.getGender());
			user.setGender(formattedGender);
		}

		// 전화번호
		if (StringUtils.hasText(requestDto.getPhoneNumber())) {
			// TODO: 전화번호 형식 유효성 검사 추가 (예: 010-XXXX-XXXX)
			user.setPhoneNumber(requestDto.getPhoneNumber());
		}

		// 주소
		if (StringUtils.hasText(requestDto.getAddress())) {
			try {
				// TODO: 주소 코드 또는 문자열 처리 로직 필요 (현재 User 엔티티 address 필드가 Integer 이므로 변환 필요)
				user.setAddress(Integer.parseInt(requestDto.getAddress()));
			} catch (NumberFormatException e) {
				log.warn("주소 형식이 숫자가 아닙니다: {}", requestDto.getAddress());
				throw new RuntimeException("잘못된 주소 형식입니다. (숫자 코드 필요)");
			}
		}

		// 3. 변경된 엔티티 저장
		userRepository.save(user);
		log.info("마이페이지 프로필 정보 수정 완료 - 사용자 ID: {}", userId);
	}

	// === Private 유틸리티 메서드들 ===

	/**
	 * MyPageProfileResponseDto 객체 생성
	 */
	private MyPageProfileResponseDto buildMyPageProfileResponse(User user) {
		return MyPageProfileResponseDto.builder()
				.basicInfo(MyPageProfileResponseDto.BasicInfoDto.builder()
						.profileImage(user.getDisplayProfileImage())
						.name(user.getName())
						.nickname(user.getDisplayName())
						.birthday(formatBirthday(user.getBirthday()))
						.gender(formatGender(user.getGender()))
						.build())
				.contactInfo(MyPageProfileResponseDto.ContactInfoDto.builder()
						.phoneNumber(user.getPhoneNumber())
						.email(user.getEmail())
						.address(formatAddress(user.getAddress()))
						.build())
				.build();
	}

	/**
	 * 생년월일 포맷팅 (소셜 로그인 사용자의 경우 provider 정보가 저장되어 있을 수 있음)
	 */
	private String formatBirthday(String birthday) {
		if (!StringUtils.hasText(birthday)) {
			return null;
		}

		// 소셜 로그인 제공자 정보인 경우
		if ("google".equals(birthday) || "kakao".equals(birthday) || "naver".equals(birthday)) {
			return null;
		}

		try {
			// YYYY-MM-DD 형식인 경우 YYYY.MM.DD로 변환
			if (birthday.contains("-")) {
				LocalDate date = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
			}
			return birthday;
		} catch (DateTimeParseException e) {
			log.warn("생년월일 포맷 변환 실패: {}", birthday);
			return birthday;
		}
	}

	/**
	 * 성별 정보 포맷팅
	 */
	private String formatGender(String gender) {
		if (!StringUtils.hasText(gender)) {
			return "미설정";
		}

		return switch (gender.toLowerCase()) {
			case "m", "male", "남", "남성" -> "남성";
			case "f", "female", "여", "여성" -> "여성";
			default -> gender;
		};
	}

	/**
	 * 성별 정보 포맷팅 (업데이트 시 User 엔티티에 저장할 값)
	 * 클라이언트에서 '남성' 또는 '여성'으로 보낼 것으로 예상
	 */
	private String formatGenderForUpdate(String gender) {
		if (!StringUtils.hasText(gender)) {
			return null; // 또는 기본값
		}
		return switch (gender) {
			case "남성" -> "남성";
			case "여성" -> "여성";
			default -> gender; // 예상치 못한 값이 오면 그대로 저장
		};
	}

	/**
	 * 휴대폰 번호 마스킹 처리
	 */
	private String maskPhoneNumber(String phoneNumber) {
		if (!StringUtils.hasText(phoneNumber)) {
			return null;
		}

		// 010-1234-5678 -> 010-1234-****
		if (phoneNumber.length() >= 8) {
			int lastHyphenIndex = phoneNumber.lastIndexOf("-");
			if (lastHyphenIndex > 0) {
				return phoneNumber.substring(0, lastHyphenIndex + 1) + "****";
			} else {
				// 하이픈이 없는 경우 뒤 4자리 마스킹
				return phoneNumber.substring(0, phoneNumber.length() - 4) + "****";
			}
		}

		return phoneNumber;
	}

	/**
	 * 주소 정보 포맷팅
	 */
	private String formatAddress(Integer address) {
		if (address == null) {
			return "주소를 입력하세요...";
		}

		// TODO: 주소 코드에 따른 실제 주소명 매핑 구현 필요
		return address.toString();
	}
}
