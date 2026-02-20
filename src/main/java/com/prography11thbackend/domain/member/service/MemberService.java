package com.prography11thbackend.domain.member.service;

import com.prography11thbackend.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {

    Member register(String loginId, String rawPassword, String name, Long cohortId, String part, Long teamId);

    Optional<Member> getMember(Long memberId);

    List<Member> getAllMembers();

    Member updateMember(Long memberId, String name);

    Member withdraw(Long memberId);
}
