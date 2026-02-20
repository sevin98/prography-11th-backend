package com.prography11thbackend.domain.session.repository;

import com.prography11thbackend.domain.session.entity.Session;
import com.prography11thbackend.domain.session.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCohortIdAndStatusNot(Long cohortId, SessionStatus status);

    List<Session> findByCohortId(Long cohortId);
}
