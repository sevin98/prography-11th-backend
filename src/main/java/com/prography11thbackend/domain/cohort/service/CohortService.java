package com.prography11thbackend.domain.cohort.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;

import java.util.List;
import java.util.Optional;

public interface CohortService {

    List<Cohort> getAllCohorts();

    Optional<Cohort> getCohortById(Long id);

    Optional<Cohort> getCohortByNumber(Integer number);

    CohortMember createCohortMember(Long memberId, Long cohortId, String part, Long teamId);
}
