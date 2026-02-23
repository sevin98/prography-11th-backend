package com.prography11thbackend.domain.member.service;

import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
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
    private final CohortMemberRepository cohortMemberRepository;
    private final TeamRepository teamRepository;

    @Override
    public Member register(String loginId, String rawPassword, String name, String phone, Long cohortId, Long partId, Long teamId) {

        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        Member member = Member.builder()
                .loginId(loginId)
                .passwordHash(passwordHash)
                .name(name)
                .phone(phone)
                .role(MemberRole.MEMBER)
                .status(MemberStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);

        // 기수에 배정
        cohortService.createCohortMember(savedMember.getId(), cohortId, partId, teamId);

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
    public Member updateMember(Long memberId, String name, String phone, Long cohortId, Long partId, Long teamId) {
        Member member = memberRepository.findActiveById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // name, phone 업데이트
        if (name != null || phone != null) {
            String newName = name != null ? name : member.getName();
            String newPhone = phone != null ? phone : member.getPhone();
            member.update(newName, newPhone);
        }

        // cohortId, partId, teamId 업데이트
        if (cohortId != null) {
            // 해당 기수의 CohortMember 조회
            var existingCohortMemberOpt = cohortMemberRepository.findByMemberIdAndCohortId(memberId, cohortId);
            
            if (existingCohortMemberOpt.isPresent()) {
                // 기존 CohortMember가 있으면 partId, teamId 업데이트
                var cohortMember = existingCohortMemberOpt.get();
                
                // partId 업데이트
                if (partId != null) {
                    com.prography11thbackend.domain.cohort.entity.Part[] parts = com.prography11thbackend.domain.cohort.entity.Part.values();
                    if (partId >= 6 && partId < 6 + parts.length) {
                        cohortMember.updatePart(parts[(int)(partId - 6)]);
                    } else {
                        throw new BusinessException(ErrorCode.PART_NOT_FOUND);
                    }
                }
                
                // teamId 업데이트
                if (teamId != null) {
                    var team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
                    cohortMember.updateTeam(team);
                }
            } else {
                // 기존 CohortMember가 없으면 새로 생성
                // partId는 필수이므로 null이면 예외 발생
                if (partId == null) {
                    throw new BusinessException(ErrorCode.PART_NOT_FOUND);
                }
                cohortService.createCohortMember(memberId, cohortId, partId, teamId);
            }
        }

        return member;
    }

    @Override
    public Member withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == com.prography11thbackend.domain.member.entity.MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }

        if (member.getStatus() != com.prography11thbackend.domain.member.entity.MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        member.withdraw();

        return member;
    }
}
