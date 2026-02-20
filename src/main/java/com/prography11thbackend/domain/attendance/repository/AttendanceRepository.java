package com.prography11thbackend.domain.attendance.repository;

import com.prography11thbackend.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByMemberIdAndSessionId(Long memberId, Long sessionId);

    List<Attendance> findByMemberId(Long memberId);

    List<Attendance> findBySessionId(Long sessionId);
}
