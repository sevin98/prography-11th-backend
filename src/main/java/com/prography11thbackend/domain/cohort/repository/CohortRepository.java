package com.prography11thbackend.domain.cohort.repository;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CohortRepository extends JpaRepository<Cohort, Long> {

    Optional<Cohort> findByNumber(Integer number);
}
