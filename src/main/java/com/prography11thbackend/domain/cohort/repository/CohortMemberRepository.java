package com.prography11thbackend.domain.cohort.repository;

import com.prography11thbackend.domain.cohort.entity.CohortMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CohortMemberRepository extends JpaRepository<CohortMember, Long> {

    Optional<CohortMember> findByMemberIdAndCohortId(Long memberId, Long cohortId);

    List<CohortMember> findByCohortId(Long cohortId);

    List<CohortMember> findByMemberId(Long memberId);
}
