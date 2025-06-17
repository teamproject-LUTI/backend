package com.luti.travel.exception;

import com.amadeus.Response;
import com.amadeus.exceptions.ResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

/** 외부 API 전용 런타임 예외 */
public class ExternalApiException extends RuntimeException {

    /** Amadeus·ChatGPT 식별용 타입-세이프 enum */
    public enum ApiSource { AMADEUS, CHATGPT }

    private final ApiSource source;
    private final HttpStatus status;

    public ExternalApiException(ApiSource  source,
                                HttpStatus status,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.source  = source;
        this.status  = status;
    }

    /* ─────────────  정적 팩토리 메서드들 ───────────── */

    /** Amadeus SDK 7.0.0 전용 */
    public static ExternalApiException ofAmadeus(ResponseException ex, String op) {
        int raw = Optional.ofNullable(ex.getResponse())
                .map(Response::getStatusCode)
                .orElse(500);
        return new ExternalApiException(
                ApiSource.AMADEUS,
                toStatus(raw),
                op + " 실패: " + ex.getMessage(),
                ex
        );
    }

    /** OpenAI(ChatGPT) WebClient 오류 전용 */
    public static ExternalApiException ofChatGpt(WebClientResponseException ex, String op) {
        return new ExternalApiException(
                ApiSource.CHATGPT,
                (HttpStatus) ex.getStatusCode(),
                op + " 실패: " + ex.getResponseBodyAsString(),
                ex
        );
    }

    /** 그 밖의 런타임 예외를 감싸는 헬퍼 */
    public static ExternalApiException ofGeneric(ApiSource source, String op, Exception ex) { // NEW
        return new ExternalApiException(
                source,
                HttpStatus.INTERNAL_SERVER_ERROR,
                op + " 실패: " + ex.getMessage(),
                ex
        );
    }

    /* ─────────────  공통 util ───────────── */
    private static HttpStatus toStatus(int code) {
        return Optional.ofNullable(HttpStatus.resolve(code))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* ─────────────  getter ───────────── */
    public ApiSource  getSource() { return source; }
    public HttpStatus getStatus() { return status; }
}
