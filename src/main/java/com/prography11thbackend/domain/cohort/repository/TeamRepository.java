package com.prography11thbackend.domain.cohort.repository;

import com.prography11thbackend.domain.cohort.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
