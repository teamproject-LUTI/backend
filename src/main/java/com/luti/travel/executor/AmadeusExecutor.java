package com.luti.travel.executor;

import com.amadeus.exceptions.ResponseException;
import com.luti.travel.exception.ExternalApiException;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.concurrent.Callable;   // ✅ 표준 Callable 사용

/**
 * Amadeus SDK 7.x 호출을 래핑해 모든 예외를 ExternalApiException 하나로 통일한다.
 * Supplier 대신 Callable을 사용해 checked 예외 처리 문제를 해결.
 */
public final class AmadeusExecutor {

    private AmadeusExecutor() { /* static util class */ }

    /**
     * @param op   사람이 읽기 쉬운 작업명(예: "호텔 검색")
     * @param call Amadeus SDK 호출 람다/메서드 레퍼런스 (Callable → throws Exception 허용)
     * @return     Amadeus SDK 호출 결과
     */
    public static <T> T execute(String op, Callable<T> call) {

        Objects.requireNonNull(call, "call must not be null");

        try {
            return call.call();     // Callable<T>
        }
        // Amadeus SDK ResponseException → 통일 예외
        catch (ResponseException e) {
            throw ExternalApiException.ofAmadeus(e, op);
        }
        // 이미 변환된 예외는 그대로 전파
        catch (ExternalApiException e) {
            throw e;
        }
        // 나머지 모든 예외도 통일
        catch (Exception e) {
            throw new ExternalApiException(
                    ExternalApiException.ApiSource.AMADEUS,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    op + " 실패: " + e.getMessage(),
                    e
            );
        }
    }
}
