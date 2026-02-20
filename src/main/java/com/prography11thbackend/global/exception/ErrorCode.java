package com.prography11thbackend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth
    LOGIN_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "로그인 ID를 찾을 수 없습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 일치하지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
    MEMBER_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER_002", "탈퇴한 회원입니다."),
    LOGIN_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_003", "이미 존재하는 로그인 ID입니다."),

    // Cohort
    COHORT_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_001", "기수를 찾을 수 없습니다."),
    COHORT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_002", "기수 회원 정보를 찾을 수 없습니다."),

    // Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_001", "일정을 찾을 수 없습니다."),
    SESSION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "SESSION_002", "진행 중인 일정이 아닙니다."),
    SESSION_CANCELLED(HttpStatus.BAD_REQUEST, "SESSION_003", "취소된 일정은 수정할 수 없습니다."),

    // QR Code
    QR_INVALID(HttpStatus.BAD_REQUEST, "QR_001", "유효하지 않은 QR 코드입니다."),
    QR_EXPIRED(HttpStatus.BAD_REQUEST, "QR_002", "만료된 QR 코드입니다."),
    QR_NOT_FOUND(HttpStatus.NOT_FOUND, "QR_003", "QR 코드를 찾을 수 없습니다."),

    // Attendance
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_001", "출결 정보를 찾을 수 없습니다."),
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.CONFLICT, "ATTENDANCE_002", "이미 출결 체크가 완료되었습니다."),
    EXCUSE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ATTENDANCE_003", "공결 횟수가 초과되었습니다."),

    // Deposit
    DEPOSIT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "DEPOSIT_001", "보증금 잔액이 부족합니다."),
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPOSIT_002", "보증금 정보를 찾을 수 없습니다."),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 입력입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
