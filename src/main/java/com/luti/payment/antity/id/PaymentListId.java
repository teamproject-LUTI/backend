package com.luti.luti.payment.antity.id;

import java.io.Serializable;
import java.util.Objects;

public class PaymentListId implements Serializable {

    private String loginId;

    private Integer paymentCd;

    private Integer paymentNo;

    public PaymentListId() {}

    /**
     * 두 PaymentListId 객체가 같은지 비교 (PK 값 기준 비교)
     * JPA에서 복합키 비교 시 필수로 사용됨
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentListId)) return false;
        PaymentListId that = (PaymentListId) o;
        return Objects.equals(loginId, that.loginId) &&
                Objects.equals(paymentCd, that.paymentCd) &&
                Objects.equals(paymentNo, that.paymentNo);
    }

    /**
     * equals와 함께 사용되는 hashCode 구현
     * 동일한 값이면 동일한 해시코드 반환 보장
     */
    @Override
    public int hashCode() {
        return Objects.hash(loginId, paymentCd, paymentNo);
    }
}
