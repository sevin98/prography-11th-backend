package com.prography11thbackend.domain.member.repository;

import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId( String loginId );

    boolean existsByLoginId( String loginId );

    Optional<Member> findByIdAndStatus( Long id, MemberStatus status );

    Optional<Member> findByLoginIdAndStatus( String loginId, MemberStatus status );

    default Optional<Member> findActiveById( Long id ) {
        return findByIdAndStatus(id, MemberStatus.ACTIVE);
    }

    default Optional<Member> findActiveByLoginId( String loginId ) {
        return findByLoginIdAndStatus(loginId, MemberStatus.ACTIVE);
    }
}
