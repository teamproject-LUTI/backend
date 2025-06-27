package com.luti.mypage.entity;

import com.luti.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "travel_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TravelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "travel_record_id", updatable = false, nullable = false)
	private Long travelRecordId;

	// ✅ Route와 동일한 패턴으로 User 관계 매핑 추가
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	private User userId;

	@Column(name = "payment_cd")
	private Integer paymentCd;

	@Column(name = "payment_id")
	private Long paymentId;

	@Column(name = "travel_title", length = 200)
	private String travelTitle;

	@Lob
	@Column(name = "travel_content", columnDefinition = "json")
	private String travelContent;

	// 생성 시간 추가 (선택사항)
	@Column(name = "created_at")
	private java.time.LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = java.time.LocalDateTime.now();
	}
}