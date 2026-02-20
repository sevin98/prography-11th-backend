package com.prography11thbackend.domain.deposit.entity;

import com.prography11thbackend.domain.deposit.entity.Deposit;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deposit_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", nullable = false)
    private Deposit deposit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositHistoryType type;

    @Column(nullable = false)
    private Integer amount; // 변동 금액

    @Column(nullable = false)
    private Integer balanceAfter; // 변동 후 잔액

    @Column
    private String description; // 설명

    @Builder
    public DepositHistory(Deposit deposit, DepositHistoryType type, Integer amount, Integer balanceAfter, String description) {
        this.deposit = deposit;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }
}
