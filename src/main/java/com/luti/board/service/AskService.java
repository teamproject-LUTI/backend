package com.luti.board.service;

import com.luti.auth.entity.User;
import com.luti.auth.enums.UserTypeEnum;
import com.luti.auth.repository.UserRepository;
import com.luti.board.dto.AskRequestDto;
import com.luti.board.dto.AskResponseDto;
import com.luti.board.entity.Ask;
import com.luti.board.repository.AskRepository;
import com.luti.board.repository.MyAskSearchDto;
import com.luti.dto.MultiResponseDto;
import com.luti.dto.SingleResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AskService {

	private final AskRepository askRepository;
	private final UserRepository userRepository;

	/**
	 * 권한 체크 유틸리티 메서드
	 * 작성자이거나 관리자인지 확인합니다.
	 */
	private boolean hasPermissionToModify(Ask ask, Long userId) {
		// 작성자인지 확인
		if (ask.getUser().getUserId().equals(userId)) {
			return true;
		}

		// 관리자인지 확인
		return userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());
	}

	/**
	 * 문의글 목록 조회 (페이징 + 검색)
	 *
	 * @param page            1-based page 번호
	 * @param size            페이지 사이즈
	 * @param userId   현재 로그인된 사용자의 ID
	 * @param searchType      검색 타입 (author, title, content)
	 * @param keyword         검색어
	 */
	public MultiResponseDto<AskResponseDto> getAsks(int page, int size, Long userId, String searchType, String keyword) {
		// 1) Specification 초기화 : 아무 조건이 없는 상태로 시작
		Specification<Ask> spec = Specification.allOf();

		// 2) 검색어(keyword)가 존재하면 검색조건(searchType)에 따라 동적 조건 추가
		if (keyword != null && !keyword.isBlank()) {
			switch (searchType) {
				case "author":
					spec = spec.and((root, query, cb) ->
							cb.like(root.get("user").get("nickname"), "%" + keyword + "%"));
					break;
				case "title":
					spec = spec.and((root, query, cb) ->
							cb.like(root.get("title"), "%" + keyword + "%"));
					break;
				case "content":
					spec = spec.and((root, query, cb) ->
							cb.like(root.get("content"), "%" + keyword + "%"));
					break;
				default:
					throw new IllegalArgumentException("Unknown searchType: " + searchType);
			}
		}

		//페이징, 정렬 : 최신순(내림차순)
		//spec과 페이징,정렬 정보를 한번에 넘겨서 조회
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Ask> asks = askRepository.findAll(spec, pageable);

		// 관리자 여부 확인
		boolean isAdmin = userId != null && userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

		// owner 플래그를 셋팅하면서 DTO 변환
		var dtos = asks.stream()
				.map(ask -> AskResponseDto.of(ask, userId, isAdmin))
				.collect(Collectors.toList());

		return new MultiResponseDto<>(dtos, asks);
	}

	/**
	 * 단일 문의글 조회
	 *
	 * @param askId           조회할 문의글 ID
	 * @param userId   현재 로그인된 사용자의 ID
	 */
	@Transactional
	public SingleResponseDto<AskResponseDto> getAsk(Long askId, Long userId) {
		Ask ask = askRepository.findById(askId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

		// 조회수 증가 (Ask 엔티티에 viewCount 필드가 있다면)
		// ask.incrementViewCount();

		// 관리자 여부 확인
		boolean isAdmin = userId != null && userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

		return new SingleResponseDto<>(AskResponseDto.of(ask, userId, isAdmin));
	}

	/**
	 * 문의글 등록
	 *
	 * @param userId  작성자 User ID (현재 로그인된 사용자)
	 * @param dto     요청 DTO
	 */
	@Transactional
	public SingleResponseDto<AskResponseDto> createAsk(Long userId, AskRequestDto dto) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));

		Ask ask = Ask.builder()
				.user(user)
				.title(dto.getTitle())
				.content(dto.getContent())
				.build();

		Ask saved = askRepository.save(ask);

		// 관리자 여부 확인
		boolean isAdmin = userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

		// 등록 후에도 본인이 작성자이므로 owner=true
		return new SingleResponseDto<>(AskResponseDto.of(saved, userId, isAdmin));
	}

	/**
	 * 문의글 수정
	 *
	 * @param askId           수정할 문의글 ID
	 * @param dto             수정할 데이터 DTO
	 * @param userId   현재 로그인된 사용자의 ID
	 */
	@Transactional
	public SingleResponseDto<AskResponseDto> updateAsk(Long askId,
													   AskRequestDto dto,
													   Long userId) {
		Ask ask = askRepository.findById(askId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

		// 권한 체크: 작성자이거나 관리자인지 확인
		if (!hasPermissionToModify(ask, userId)) {
			throw new AccessDeniedException("Access denied: Only author or admin can modify this ask");
		}

		ask.setTitle(dto.getTitle());
		ask.setContent(dto.getContent());
		// Auditable이 자동으로 modifiedAt 갱신

		// 관리자 여부 확인
		boolean isAdmin = userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

		return new SingleResponseDto<>(AskResponseDto.of(ask, userId, isAdmin));
	}

	/**
	 * 문의글 삭제
	 *
	 * @param askId           삭제할 문의글 ID
	 * @param userId   현재 로그인된 사용자의 ID
	 */
	@Transactional
	public void deleteAsk(Long askId, Long userId) {
		Ask ask = askRepository.findById(askId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

		// 권한 체크: 작성자이거나 관리자인지 확인
		if (!hasPermissionToModify(ask, userId)) {
			throw new AccessDeniedException("Access denied: Only author or admin can delete this ask");
		}

		askRepository.delete(ask);
	}

	/**
	 * 내 문의글 조회 (정렬 + 답변 상태 필터 적용)
	 *
	 * @param userId 사용자 ID
	 * @param searchDto 검색 조건
	 * @return 페이지네이션이 적용된 내 문의글 목록
	 */
	public MultiResponseDto<AskResponseDto> getMyAsks(Long userId, MyAskSearchDto searchDto) {
		log.info("내 문의글 조회 - 사용자 ID: {}, 검색 조건: {}", userId, searchDto);

		// Pageable 객체 생성
		Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());

		// 검색 조건에 따라 적절한 Repository 메서드 호출
		Page<Ask> askPage = findAsksByCondition(userId, searchDto, pageable);

		// 관리자 여부 확인
		boolean isAdmin = userRepository.isUserAdmin(userId, UserTypeEnum.ADMIN.getId());

		// Ask 엔티티를 AskResponseDto로 변환
		var dtos = askPage.stream()
				.map(ask -> AskResponseDto.of(ask, userId, isAdmin)) // owner는 항상 true
				.collect(Collectors.toList());

		return new MultiResponseDto<>(dtos, askPage);
	}

	/**
	 * 문의글에 댓글이 달렸을 때 답변 상태를 "답변 완료"로 변경
	 *
	 * @param askId 문의글 ID
	 */
	@Transactional
	public void markAsAnswered(Long askId) {
		log.info("문의글 답변 상태 업데이트 - askId: {}", askId);

		Ask ask = askRepository.findById(askId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

		// 이미 답변 완료 상태가 아닌 경우에만 업데이트
		if (!ask.getAnswered()) {
			ask.markAnswered();
			log.info("문의글 답변 상태가 '답변 완료'로 변경되었습니다. askId: {}", askId);
		} else {
			log.info("문의글이 이미 답변 완료 상태입니다. askId: {}", askId);
		}
	}

	/**
	 * 문의글 답변 상태를 수동으로 변경 (관리자용)
	 *
	 * @param askId 문의글 ID
	 * @param answered 답변 상태 (true: 답변완료, false: 답변대기)
	 */
	@Transactional
	public void updateAnswerStatus(Long askId, boolean answered) {
		log.info("문의글 답변 상태 수동 변경 - askId: {}, answered: {}", askId, answered);

		Ask ask = askRepository.findById(askId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다. id=" + askId));

		ask.setAnswered(answered);
		log.info("문의글 답변 상태가 변경되었습니다. askId: {}, answered: {}", askId, answered);
	}

	/**
	 * 검색 조건에 따라 적절한 Repository 메서드를 호출하는 헬퍼 메서드
	 */
	private Page<Ask> findAsksByCondition(Long userId, MyAskSearchDto searchDto, Pageable pageable) {
		String sortBy = searchDto.getSortBy();
		String answerStatus = searchDto.getAnswerStatus();

		// 답변 상태 필터가 없는 경우 (전체)
		if ("all".equals(answerStatus)) {
			if ("oldest".equals(sortBy)) {
				return askRepository.findByUserUserIdOrderByCreatedAtAsc(userId, pageable);
			} else {
				return askRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
			}
		}

		// 답변 상태 필터가 있는 경우
		Boolean answered = "answered".equals(answerStatus);

		if ("oldest".equals(sortBy)) {
			return askRepository.findByUserUserIdAndAnsweredOrderByCreatedAtAsc(userId, answered, pageable);
		} else {
			return askRepository.findByUserUserIdAndAnsweredOrderByCreatedAtDesc(userId, answered, pageable);
		}
	}
}