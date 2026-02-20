package com.prography11thbackend.domain.attendance.service;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.domain.qrcode.entity.QRCode;
import com.prography11thbackend.domain.qrcode.repository.QRCodeRepository;
import com.prography11thbackend.api.attendance.dto.AttendanceSummaryResponse;
import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;
import com.prography11thbackend.domain.session.repository.SessionRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Integer CURRENT_COHORT_NUMBER = 11;
    private static final Integer MAX_EXCUSE_COUNT = 3;
    private static final Integer PENALTY_PER_MINUTE = 500;
    private static final Integer MAX_LATE_PENALTY = 10_000;
    private static final Integer ABSENT_PENALTY = 10_000;

    private final AttendanceRepository attendanceRepository;
    private final QRCodeRepository qrCodeRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;
    private final com.prography11thbackend.domain.session.repository.SessionRepository sessionRepository;

    @Override
    public Attendance checkAttendanceByQR(String qrHashValue, Long memberId) {
        // 1. QR hashValue 유효성 검증
        QRCode qrCode = qrCodeRepository.findByHashValue(qrHashValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_INVALID));

        // 2. QR 만료 여부 검증
        if (qrCode.isExpired()) {
            throw new BusinessException(ErrorCode.QR_EXPIRED);
        }

        Session session = qrCode.getSession();

        // 3. 일정 상태가 IN_PROGRESS인지 검증
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }

        // 4. 회원 존재 여부 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 5. 회원 탈퇴 여부 검증
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }

        // 6. 중복 출결 여부 검증
        if (attendanceRepository.findByMemberIdAndSessionId(memberId, session.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        // 7. 기수 회원 정보 존재 여부 검증
        CohortMember cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(memberId, session.getCohort().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        // 검증 통과 후 출결 처리
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status;
        Integer penalty;

        // 지각 판정: 일정 시작 시간 기준으로 현재 시각이 이후이면 LATE, 이전이면 PRESENT
        if (now.isAfter(session.getStartTime())) {
            status = AttendanceStatus.LATE;
            long minutesLate = Duration.between(session.getStartTime(), now).toMinutes();
            penalty = (int) Math.min(minutesLate * PENALTY_PER_MINUTE, MAX_LATE_PENALTY);
        } else {
            status = AttendanceStatus.PRESENT;
            penalty = 0;
        }

        Attendance attendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(status)
                .penalty(penalty)
                .checkedAt(now)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        // 패널티 발생 시 보증금 자동 차감
        if (penalty > 0) {
            depositService.deductPenalty(memberId, penalty, "출석 체크 패널티");
        }

        return savedAttendance;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getMyAttendances(Long memberId) {
        return attendanceRepository.findByMemberId(memberId);
    }

    @Override
    public Attendance createAttendance(Long memberId, Long sessionId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status);

        // 공결 횟수 체크
        if (attendanceStatus == AttendanceStatus.EXCUSED) {
            checkExcuseLimit(memberId, session.getCohort().getId());
        }

        Integer penalty = calculatePenalty(attendanceStatus, session.getStartTime(), LocalDateTime.now());

        Attendance attendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(attendanceStatus)
                .penalty(penalty)
                .checkedAt(LocalDateTime.now())
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        if (penalty > 0) {
            depositService.deductPenalty(memberId, penalty, "관리자 출결 등록 패널티");
        }

        return savedAttendance;
    }

    @Override
    public Attendance updateAttendance(Long attendanceId, String newStatus) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        AttendanceStatus oldStatus = attendance.getStatus();
        Integer oldPenalty = attendance.getPenalty();
        AttendanceStatus newStatusEnum = AttendanceStatus.valueOf(newStatus);

        // 공결 횟수 관리
        manageExcuseCount(attendance.getMember().getId(), attendance.getSession().getId(), oldStatus, newStatusEnum);

        Integer newPenalty = calculatePenalty(newStatusEnum, attendance.getSession().getStartTime(), attendance.getCheckedAt());
        Integer penaltyDifference = newPenalty - oldPenalty;

        attendance.updateStatus(newStatusEnum, newPenalty);

        // 보증금 자동 조정
        if (penaltyDifference > 0) {
            // 패널티 증가 → 추가 차감
            depositService.deductPenalty(attendance.getMember().getId(), penaltyDifference, "출결 수정 패널티");
        } else if (penaltyDifference < 0) {
            // 패널티 감소 → 환급
            depositService.refundPenalty(attendance.getMember().getId(), Math.abs(penaltyDifference), "출결 수정 환급");
        }

        return attendance;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendancesBySession(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendancesByMember(Long memberId) {
        return attendanceRepository.findByMemberId(memberId);
    }

    private Integer calculatePenalty(AttendanceStatus status, LocalDateTime sessionStartTime, LocalDateTime checkedAt) {
        switch (status) {
            case PRESENT:
                return 0;
            case ABSENT:
                return ABSENT_PENALTY;
            case LATE:
                if (sessionStartTime != null && checkedAt != null) {
                    long minutesLate = Duration.between(sessionStartTime, checkedAt).toMinutes();
                    return (int) Math.min(minutesLate * PENALTY_PER_MINUTE, MAX_LATE_PENALTY);
                }
                return 0;
            case EXCUSED:
                return 0;
            default:
                return 0;
        }
    }

    private void checkExcuseLimit(Long memberId, Long cohortId) {
        CohortMember cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        if (cohortMember.getExcuseCount() >= MAX_EXCUSE_COUNT) {
            throw new BusinessException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
        }
    }

    private void manageExcuseCount(Long memberId, Long sessionId, AttendanceStatus oldStatus, AttendanceStatus newStatus) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        CohortMember cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(memberId, session.getCohort().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        // 다른 상태 → EXCUSED: 공결 횟수 +1
        if (oldStatus != AttendanceStatus.EXCUSED && newStatus == AttendanceStatus.EXCUSED) {
            cohortMember.increaseExcuseCount();
        }
        // EXCUSED → 다른 상태: 공결 횟수 -1
        else if (oldStatus == AttendanceStatus.EXCUSED && newStatus != AttendanceStatus.EXCUSED) {
            cohortMember.decreaseExcuseCount();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummaryByMember(Long memberId) {
        List<Attendance> attendances = attendanceRepository.findByMemberId(memberId);
        
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int excusedCount = 0;
        int totalPenalty = 0;

        for (Attendance attendance : attendances) {
            switch (attendance.getStatus()) {
                case PRESENT:
                    presentCount++;
                    break;
                case LATE:
                    lateCount++;
                    totalPenalty += attendance.getPenalty();
                    break;
                case ABSENT:
                    absentCount++;
                    totalPenalty += attendance.getPenalty();
                    break;
                case EXCUSED:
                    excusedCount++;
                    break;
            }
        }

        return new AttendanceSummaryResponse(
                attendances.size(),
                presentCount,
                lateCount,
                absentCount,
                excusedCount,
                totalPenalty
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummaryBySession(Long sessionId) {
        List<Attendance> attendances = attendanceRepository.findBySessionId(sessionId);
        
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int excusedCount = 0;
        int totalPenalty = 0;

        for (Attendance attendance : attendances) {
            switch (attendance.getStatus()) {
                case PRESENT:
                    presentCount++;
                    break;
                case LATE:
                    lateCount++;
                    totalPenalty += attendance.getPenalty();
                    break;
                case ABSENT:
                    absentCount++;
                    totalPenalty += attendance.getPenalty();
                    break;
                case EXCUSED:
                    excusedCount++;
                    break;
            }
        }

        return new AttendanceSummaryResponse(
                attendances.size(),
                presentCount,
                lateCount,
                absentCount,
                excusedCount,
                totalPenalty
        );
    }
}
