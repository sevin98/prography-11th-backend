package com.prography11thbackend.domain.member.service;

import com.prography11thbackend.domain.cohort.service.CohortService;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CohortService cohortService;
    private final DepositService depositService;

    @Override
    public Member register(String loginId, String rawPassword, String name, Long cohortId, String part, Long teamId) {

        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        Member member = Member.builder()
                .loginId(loginId)
                .passwordHash(passwordHash)
                .name(name)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);

        // 기수에 배정
        cohortService.createCohortMember(savedMember.getId(), cohortId, part, teamId);

        // 보증금 초기 설정
        depositService.createInitialDeposit(savedMember.getId());

        return savedMember;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> getMember(Long memberId) {
        return memberRepository.findActiveById(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public Member updateMember(Long memberId, String name) {
        Member member = memberRepository.findActiveById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.update(name);

        return member;
    }

    @Override
    public Member withdraw(Long memberId) {

        Member member = memberRepository.findActiveById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();

        return member;
    }
}
