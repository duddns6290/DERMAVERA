package com.org.dermavera.diagnosis;

/**
 * skin-ai 서버 연결 실패, 타임아웃, 5xx 응답 등으로 진단을 수행할 수 없을 때 사용.
 */
public class SkinAiUnavailableException extends RuntimeException {

    public SkinAiUnavailableException(String message) {
        super(message);
    }

    public SkinAiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
