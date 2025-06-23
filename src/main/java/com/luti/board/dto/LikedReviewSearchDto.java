package com.luti.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 좋아요 누른 리뷰글 검색 조건 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class LikedReviewSearchDto {

	/**
	 * 검색 타입 (title, content, author)
	 */
	private String searchType;

	/**
	 * 검색어
	 */
	private String keyword;

	/**
	 * 정렬 조건 (latest, oldest)
	 * latest: 최근 좋아요순 (좋아요 누른 날짜 기준)
	 * oldest: 오래된 좋아요순 (좋아요 누른 날짜 기준)
	 */
	private String sortBy = "latest";

	/**
	 * 페이지 번호 (0부터 시작)
	 */
	private int page = 0;

	/**
	 * 페이지 크기
	 */
	private int size = 10;

}
