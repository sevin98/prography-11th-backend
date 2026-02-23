package com.prography11thbackend.domain.attendance.service;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import com.prography11thbackend.domain.attendance.entity.AttendanceStatus;
import com.prography11thbackend.domain.attendance.repository.AttendanceRepository;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
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
import java.util.Optional;

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
    private final CohortRepository cohortRepository;
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
                .reason(null) // QR 체크인 시 reason은 null
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        // 패널티 발생 시 보증금 자동 차감
        if (penalty > 0) {
            depositService.deductPenalty(memberId, penalty, "출석 체크 패널티 (출결 ID: " + savedAttendance.getId() + ")");
        }

        return savedAttendance;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getMyAttendances(Long memberId) {
        return attendanceRepository.findByMemberId(memberId);
    }

    @Override
    public Attendance createAttendance(Long sessionId, Long memberId, String status, Integer lateMinutes, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        // 중복 출결 확인
        if (attendanceRepository.findByMemberIdAndSessionId(memberId, sessionId).isPresent()) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        // 기수 회원 정보 확인
        CohortMember cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(memberId, session.getCohort().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        AttendanceStatus attendanceStatus = AttendanceStatus.valueOf(status);

        // 공결 횟수 체크 및 관리
        if (attendanceStatus == AttendanceStatus.EXCUSED) {
            checkExcuseLimit(memberId, session.getCohort().getId());
            cohortMember.increaseExcuseCount();
        }

        // 패널티 계산
        Integer penalty = calculatePenaltyForManual(attendanceStatus, lateMinutes);

        Attendance attendance = Attendance.builder()
                .member(member)
                .session(session)
                .status(attendanceStatus)
                .penalty(penalty)
                .checkedAt(null) // 수동 등록 시 null
                .reason(reason)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        // 패널티 > 0이면 보증금 차감
        if (penalty > 0) {
            depositService.deductPenalty(memberId, penalty, "출결 등록 - " + attendanceStatus.name() + " 패널티 " + penalty + "원 (출결 ID: " + savedAttendance.getId() + ")");
        }

        return savedAttendance;
    }

    @Override
    public Attendance updateAttendance(Long attendanceId, String newStatus, Integer lateMinutes, String reason) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        AttendanceStatus oldStatus = attendance.getStatus();
        Integer oldPenalty = attendance.getPenalty();
        AttendanceStatus newStatusEnum = AttendanceStatus.valueOf(newStatus);

        // 기수 회원 정보 확인
        CohortMember cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(
                attendance.getMember().getId(), 
                attendance.getSession().getCohort().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        // 공결 횟수 관리
        manageExcuseCount(attendance.getMember().getId(), attendance.getSession().getId(), oldStatus, newStatusEnum);

        // 패널티 계산
        Integer newPenalty = calculatePenaltyForManual(newStatusEnum, lateMinutes);
        Integer penaltyDifference = newPenalty - oldPenalty;

        // 출결 상태 및 패널티 업데이트
        attendance.updateStatus(newStatusEnum, newPenalty);
        
        // reason 업데이트 (전달된 경우)
        if (reason != null) {
            attendance.updateReason(reason);
        }

        // 보증금 자동 조정
        if (penaltyDifference > 0) {
            // 패널티 증가 → 추가 차감
            depositService.deductPenalty(attendance.getMember().getId(), penaltyDifference, "출결 수정 - 패널티 " + penaltyDifference + "원 (출결 ID: " + attendance.getId() + ")");
        } else if (penaltyDifference < 0) {
            // 패널티 감소 → 환급
            depositService.refundPenalty(attendance.getMember().getId(), Math.abs(penaltyDifference), "출결 수정 - 환급 " + Math.abs(penaltyDifference) + "원 (출결 ID: " + attendance.getId() + ")");
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

    private Integer calculatePenaltyForManual(AttendanceStatus status, Integer lateMinutes) {
        switch (status) {
            case PRESENT:
                return 0;
            case ABSENT:
                return ABSENT_PENALTY;
            case LATE:
                if (lateMinutes != null && lateMinutes > 0) {
                    return (int) Math.min(lateMinutes * PENALTY_PER_MINUTE, MAX_LATE_PENALTY);
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
        // 회원 존재 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

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

        // 현재 기수(11기)의 보증금 조회
        Integer deposit = null;
        try {
            com.prography11thbackend.domain.cohort.entity.Cohort currentCohort = cohortRepository.findByNumber(CURRENT_COHORT_NUMBER)
                    .orElse(null);
            
            if (currentCohort != null) {
                Optional<com.prography11thbackend.domain.cohort.entity.CohortMember> cohortMemberOpt = 
                        cohortMemberRepository.findByMemberIdAndCohortId(memberId, currentCohort.getId());
                
                if (cohortMemberOpt.isPresent()) {
                    deposit = depositService.findDepositByMemberId(memberId)
                            .map(com.prography11thbackend.domain.deposit.entity.Deposit::getBalance)
                            .orElse(null);
                }
            }
        } catch (Exception e) {
            deposit = null;
        }

        return new AttendanceSummaryResponse(
                memberId,
                presentCount,
                absentCount,
                lateCount,
                excusedCount,
                totalPenalty,
                deposit
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.prography11thbackend.api.attendance.dto.MemberAttendanceSummaryResponse> getAttendanceSummaryBySession(Long sessionId) {
        // 일정 존재 확인
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        // 현재 기수(11기)의 전체 CohortMember 목록 조회
        com.prography11thbackend.domain.cohort.entity.Cohort currentCohort = cohortRepository.findByNumber(CURRENT_COHORT_NUMBER)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));

        List<CohortMember> cohortMembers = cohortMemberRepository.findByCohortId(currentCohort.getId());

        return cohortMembers.stream()
                .map(cohortMember -> {
                    Long memberId = cohortMember.getMember().getId();
                    List<Attendance> memberAttendances = attendanceRepository.findByMemberId(memberId);

                    int presentCount = 0;
                    int absentCount = 0;
                    int lateCount = 0;
                    int excusedCount = 0;
                    int totalPenalty = 0;

                    for (Attendance attendance : memberAttendances) {
                        switch (attendance.getStatus()) {
                            case PRESENT:
                                presentCount++;
                                break;
                            case ABSENT:
                                absentCount++;
                                totalPenalty += attendance.getPenalty();
                                break;
                            case LATE:
                                lateCount++;
                                totalPenalty += attendance.getPenalty();
                                break;
                            case EXCUSED:
                                excusedCount++;
                                break;
                        }
                    }

                    // 보증금 조회
                    Integer deposit = depositService.findDepositByMemberId(memberId)
                            .map(com.prography11thbackend.domain.deposit.entity.Deposit::getBalance)
                            .orElse(0);

                    return new com.prography11thbackend.api.attendance.dto.MemberAttendanceSummaryResponse(
                            memberId,
                            cohortMember.getMember().getName(),
                            presentCount,
                            absentCount,
                            lateCount,
                            excusedCount,
                            totalPenalty,
                            deposit
                    );
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.prography11thbackend.api.attendance.dto.MemberAttendanceDetailResponse getMemberAttendanceDetail(Long memberId) {
        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 기수(11기)의 CohortMember 조회
        com.prography11thbackend.domain.cohort.entity.Cohort currentCohort = cohortRepository.findByNumber(CURRENT_COHORT_NUMBER)
                .orElse(null);

        Integer generation = null;
        String partName = null;
        String teamName = null;
        Integer deposit = null;
        Integer excuseCount = null;

        if (currentCohort != null) {
            Optional<CohortMember> cohortMemberOpt = cohortMemberRepository.findByMemberIdAndCohortId(memberId, currentCohort.getId());
            if (cohortMemberOpt.isPresent()) {
                CohortMember cohortMember = cohortMemberOpt.get();
                generation = cohortMember.getCohort().getNumber();
                partName = cohortMember.getPart() != null ? cohortMember.getPart().name() : null;
                teamName = cohortMember.getTeam() != null ? cohortMember.getTeam().getName() : null;
                excuseCount = cohortMember.getExcuseCount();

                deposit = depositService.findDepositByMemberId(memberId)
                        .map(com.prography11thbackend.domain.deposit.entity.Deposit::getBalance)
                        .orElse(null);
            }
        }

        // 전체 출결 기록 조회
        List<Attendance> attendances = attendanceRepository.findByMemberId(memberId);
        List<com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse> attendanceResponses = attendances.stream()
                .map(com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse::from)
                .collect(java.util.stream.Collectors.toList());

        return new com.prography11thbackend.api.attendance.dto.MemberAttendanceDetailResponse(
                memberId,
                member.getName(),
                generation,
                partName,
                teamName,
                deposit,
                excuseCount,
                attendanceResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public com.prography11thbackend.api.attendance.dto.SessionAttendanceListResponse getSessionAttendances(Long sessionId) {
        // 일정 존재 확인
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        // 출결 기록 조회
        List<Attendance> attendances = attendanceRepository.findBySessionId(sessionId);
        List<com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse> attendanceResponses = attendances.stream()
                .map(com.prography11thbackend.api.attendance.dto.AttendanceAdminResponse::from)
                .collect(java.util.stream.Collectors.toList());

        return new com.prography11thbackend.api.attendance.dto.SessionAttendanceListResponse(
                sessionId,
                session.getTitle(),
                attendanceResponses
        );
    }
}
