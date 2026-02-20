package com.prography11thbackend.domain.deposit.entity;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deposits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deposit extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Integer balance; // 보증금 잔액

    @Builder
    public Deposit(Member member, Integer balance) {
        this.member = member;
        this.balance = balance;
    }

    public void deduct(Integer amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("보증금 잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    public void refund(Integer amount) {
        this.balance += amount;
    }
}
