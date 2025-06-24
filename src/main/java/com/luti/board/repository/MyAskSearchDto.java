package com.luti.board.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyAskSearchDto {

	/**
	 * 정렬 조건 (latest, oldest)
	 * latest: 최신순 (작성일 기준)
	 * oldest: 오래된순 (작성일 기준)
	 */
	private String sortBy = "latest";

	/**
	 * 답변 상태 필터 (all, answered, unanswered)
	 * all: 전체
	 * answered: 답변 완료
	 * unanswered: 미답변
	 */
	private String answerStatus = "all";

	/**
	 * 페이지 번호 (0부터 시작)
	 */
	private int page = 0;

	/**
	 * 페이지 크기
	 */
	private int size = 10;

}
