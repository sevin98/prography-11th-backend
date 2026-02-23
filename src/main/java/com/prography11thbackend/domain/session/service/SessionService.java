package com.prography11thbackend.domain.session.service;

import com.prography11thbackend.domain.session.entity.Session;

import java.util.List;

public interface SessionService {

    Session createSession(String title, java.time.LocalDate date, java.time.LocalTime time, String location, Long cohortId);

    List<Session> getSessionsForMember(Long cohortId);

    List<Session> getSessionsForAdmin(Long cohortId);

    Session getSessionById(Long id);

    Session updateSession(Long id, String title, java.time.LocalDate date, java.time.LocalTime time, String location, com.prography11thbackend.domain.session.entity.SessionStatus status);

    Session deleteSession(Long id);
}
