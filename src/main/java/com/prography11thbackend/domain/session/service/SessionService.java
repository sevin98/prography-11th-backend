package com.prography11thbackend.domain.session.service;

import com.prography11thbackend.domain.session.entity.Session;

import java.util.List;

public interface SessionService {

    Session createSession(String title, String description, java.time.LocalDateTime startTime, Long cohortId);

    List<Session> getSessionsForMember(Long cohortId);

    List<Session> getSessionsForAdmin(Long cohortId);

    Session getSessionById(Long id);

    Session updateSession(Long id, String title, String description, java.time.LocalDateTime startTime);

    void deleteSession(Long id);
}
