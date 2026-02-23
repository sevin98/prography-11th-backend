package com.prography11thbackend.domain.member.entity;

import com.prography11thbackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_login_id", columnNames = "login_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

        @Id @GeneratedValue
        private Long id;

        @Column(name="login_id", nullable=false)
        private String loginId;

        @Column(name="password_hash", nullable=false)
        private String passwordHash;

        @Column(nullable=false)
        private String name;

        @Column
        private String phone;

        @Enumerated(EnumType.STRING)
        @Column(nullable=false)
        private MemberRole role;

        @Enumerated(EnumType.STRING)
        @Column(nullable=false)
        private MemberStatus status;

        @Builder
        public Member(String loginId, String passwordHash, String name, String phone, MemberRole role, MemberStatus status) {
                this.loginId = loginId;
                this.passwordHash = passwordHash;
                this.name = name;
                this.phone = phone;
                this.role = role;
                this.status = status;
        }

        public void withdraw() {
                this.status = MemberStatus.WITHDRAWN;
        }

        public void update(String name, String phone) {
                if (name != null && !name.isBlank()) {
                        this.name = name;
                }
                this.phone = phone;
        }
}
