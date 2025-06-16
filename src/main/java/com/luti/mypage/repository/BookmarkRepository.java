package com.luti.mypage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.luti.mypage.entity.Bookmark;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	/**
	 * 특정 사용자의 북마크에서 user_id를 NULL로 변경
	 *
	 * @param userId 사용자 ID
	 * @return 업데이트된 북마크 수
	 */
	@Modifying
	@Query(value = "UPDATE bookmark SET user_id = NULL WHERE user_id = :userId", nativeQuery = true)
	int setUserIdToNull(@Param("userId") Long userId);

}
