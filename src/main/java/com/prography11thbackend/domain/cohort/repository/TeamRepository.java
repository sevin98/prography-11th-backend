package com.prography11thbackend.domain.cohort.repository;

import com.prography11thbackend.domain.cohort.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByCohortIdAndName(Long cohortId, String name);
}
