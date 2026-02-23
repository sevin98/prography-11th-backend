package com.prography11thbackend.api.session.controller;

import com.prography11thbackend.api.session.dto.SessionAdminResponse;
import com.prography11thbackend.api.session.dto.SessionCreateRequest;
import com.prography11thbackend.api.session.dto.SessionUpdateRequest;
import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.qrcode.repository.QRCodeRepository;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;
import com.prography11thbackend.domain.session.service.SessionService;
import com.prography11thbackend.global.common.ApiResponse;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final SessionService sessionService;
    private final AttendanceRepository attendanceRepository;
    private final QRCodeRepository qrCodeRepository;
    private final CohortRepository cohortRepository;
    private static final Integer CURRENT_COHORT_NUMBER = 11; // 11기 고정
    
    private Long getCurrentCohortId() {
        return cohortRepository.findByNumber(CURRENT_COHORT_NUMBER)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND))
                .getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionAdminResponse>>> getSessions(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String status
    ) {
        // status 파라미터 enum 파싱 및 검증
        SessionStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = SessionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_SESSION_STATUS);
            }
        }
        
        List<Session> sessions = sessionService.getSessionsForAdmin(getCurrentCohortId());
        
        // 필터링
        final SessionStatus finalStatusEnum = statusEnum;
        List<Session> filteredSessions = sessions.stream()
                .filter(session -> {
                    // dateFrom 필터
                    if (dateFrom != null && session.getStartTime() != null) {
                        if (session.getStartTime().toLocalDate().isBefore(dateFrom)) {
                            return false;
                        }
                    }
                    
                    // dateTo 필터
                    if (dateTo != null && session.getStartTime() != null) {
                        if (session.getStartTime().toLocalDate().isAfter(dateTo)) {
                            return false;
                        }
                    }
                    
                    // status 필터
                    if (finalStatusEnum != null) {
                        if (!session.getStatus().equals(finalStatusEnum)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        List<SessionAdminResponse> responses = filteredSessions.stream()
                .map(session -> {
                    // 출결 요약 계산
                    List<Attendance> attendances = attendanceRepository.findBySessionId(session.getId());
                    int present = 0;
                    int absent = 0;
                    int late = 0;
                    int excused = 0;
                    
                    for (Attendance attendance : attendances) {
                        switch (attendance.getStatus()) {
                            case PRESENT:
                                present++;
                                break;
                            case ABSENT:
                                absent++;
                                break;
                            case LATE:
                                late++;
                                break;
                            case EXCUSED:
                                excused++;
                                break;
                        }
                    }
                    
                    SessionAdminResponse.AttendanceSummary summary = new SessionAdminResponse.AttendanceSummary(
                            present,
                            absent,
                            late,
                            excused,
                            attendances.size()
                    );
                    
                    // QR 활성 여부 확인
                    Boolean qrActive = qrCodeRepository.findBySessionIdAndIsActiveTrue(session.getId()).isPresent();
                    
                    return SessionAdminResponse.from(session, summary, qrActive);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SessionAdminResponse>> createSession(@Valid @RequestBody SessionCreateRequest request) {
        Session session = sessionService.createSession(
                request.title(),
                request.date(),
                request.time(),
                request.location(),
                getCurrentCohortId()
        );
        
        // 출결 요약 (초기값)
        SessionAdminResponse.AttendanceSummary summary = new SessionAdminResponse.AttendanceSummary(0, 0, 0, 0, 0);
        Boolean qrActive = true; // 생성 시 QR 코드가 자동 생성되므로 true
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SessionAdminResponse.from(session, summary, qrActive)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionAdminResponse>> updateSession(@PathVariable Long id, @Valid @RequestBody SessionUpdateRequest request) {
        Session session = sessionService.updateSession(
                id,
                request.title(),
                request.date(),
                request.time(),
                request.location(),
                request.status()
        );
        
        // 출결 요약 계산
        List<Attendance> attendances = attendanceRepository.findBySessionId(id);
        int present = 0;
        int absent = 0;
        int late = 0;
        int excused = 0;
        
        for (Attendance attendance : attendances) {
            switch (attendance.getStatus()) {
                case PRESENT:
                    present++;
                    break;
                case ABSENT:
                    absent++;
                    break;
                case LATE:
                    late++;
                    break;
                case EXCUSED:
                    excused++;
                    break;
            }
        }
        
        SessionAdminResponse.AttendanceSummary summary = new SessionAdminResponse.AttendanceSummary(
                present,
                absent,
                late,
                excused,
                attendances.size()
        );
        
        Boolean qrActive = qrCodeRepository.findBySessionIdAndIsActiveTrue(id).isPresent();
        
        return ResponseEntity.ok(ApiResponse.success(SessionAdminResponse.from(session, summary, qrActive)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionAdminResponse>> deleteSession(@PathVariable Long id) {
        // 삭제 전에 출결 요약을 먼저 계산 (cascade 삭제 방지)
        List<Attendance> attendances = attendanceRepository.findBySessionId(id);
        int present = 0;
        int absent = 0;
        int late = 0;
        int excused = 0;
        
        for (Attendance attendance : attendances) {
            switch (attendance.getStatus()) {
                case PRESENT:
                    present++;
                    break;
                case ABSENT:
                    absent++;
                    break;
                case LATE:
                    late++;
                    break;
                case EXCUSED:
                    excused++;
                    break;
            }
        }
        
        SessionAdminResponse.AttendanceSummary summary = new SessionAdminResponse.AttendanceSummary(
                present,
                absent,
                late,
                excused,
                attendances.size()
        );
        
        // 세션 삭제
        Session session = sessionService.deleteSession(id);
        
        Boolean qrActive = qrCodeRepository.findBySessionIdAndIsActiveTrue(id).isPresent();
        
        return ResponseEntity.ok(ApiResponse.success(SessionAdminResponse.from(session, summary, qrActive)));
    }
}
