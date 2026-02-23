package com.prography11thbackend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인 아이디 또는 비밀번호가 올바르지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
    MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "MEMBER_002", "탈퇴한 회원입니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "MEMBER_003", "이미 사용 중인 로그인 아이디입니다."),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER_004", "이미 탈퇴한 회원입니다."),

    // Cohort
    COHORT_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_001", "기수를 찾을 수 없습니다."),
    COHORT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_002", "기수 회원 정보를 찾을 수 없습니다."),
    PART_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_003", "파트를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_004", "팀을 찾을 수 없습니다."),

    // Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_001", "일정을 찾을 수 없습니다."),
    SESSION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "SESSION_002", "진행 중인 일정이 아닙니다."),
    SESSION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "SESSION_003", "이미 취소된 일정입니다."),
    INVALID_SESSION_STATUS(HttpStatus.BAD_REQUEST, "SESSION_004", "유효하지 않은 일정 상태입니다."),

    // QR Code
    QR_INVALID(HttpStatus.BAD_REQUEST, "QR_001", "유효하지 않은 QR 코드입니다."),
    QR_EXPIRED(HttpStatus.BAD_REQUEST, "QR_002", "만료된 QR 코드입니다."),
    QR_NOT_FOUND(HttpStatus.NOT_FOUND, "QR_003", "QR 코드를 찾을 수 없습니다."),
    QR_ALREADY_ACTIVE(HttpStatus.CONFLICT, "QR_004", "이미 활성화된 QR 코드가 있습니다."),

    // Attendance
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_001", "출결 기록을 찾을 수 없습니다."),
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.CONFLICT, "ATTENDANCE_002", "이미 출결 체크가 완료되었습니다."),
    EXCUSE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ATTENDANCE_003", "공결 횟수를 초과했습니다 (최대 3회)"),

    // Deposit
    DEPOSIT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "DEPOSIT_001", "보증금 잔액이 부족합니다."),
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPOSIT_002", "보증금 정보를 찾을 수 없습니다."),

    // Common
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_002", "입력값이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
