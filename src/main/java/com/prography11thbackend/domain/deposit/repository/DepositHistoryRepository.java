package com.prography11thbackend.domain.deposit.repository;

import com.prography11thbackend.domain.deposit.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {

    List<DepositHistory> findByDepositIdOrderByCreatedAtDesc(Long depositId);
}
