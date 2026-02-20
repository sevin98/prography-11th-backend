package com.prography11thbackend.global.config;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.entity.MemberRole;
import com.prography11thbackend.domain.member.entity.MemberStatus;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepositService depositService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("시드 데이터 초기화 시작");

        // 기수 생성
        Cohort cohort10 = createCohortIfNotExists(10);
        Cohort cohort11 = createCohortIfNotExists(11);

        // 파트는 기수별로 자동 생성되므로 별도 처리 불필요

        // 11기 팀 생성
        Team teamA = createTeamIfNotExists("Team A", cohort11);
        Team teamB = createTeamIfNotExists("Team B", cohort11);
        Team teamC = createTeamIfNotExists("Team C", cohort11);

        // 관리자 생성
        Member admin = createAdminIfNotExists();

        // 관리자 기수 배정 (11기)
        createCohortMemberIfNotExists(admin, cohort11, Part.SERVER, null);

        // 관리자 보증금 설정
        if (depositService.findDepositByMemberId(admin.getId()).isEmpty()) {
            depositService.createInitialDeposit(admin.getId());
        }

        log.info("시드 데이터 초기화 완료");
    }

    private Cohort createCohortIfNotExists(Integer number) {
        return cohortRepository.findByNumber(number)
                .orElseGet(() -> {
                    Cohort cohort = Cohort.builder()
                            .number(number)
                            .build();
                    return cohortRepository.save(cohort);
                });
    }

    private Team createTeamIfNotExists(String name, Cohort cohort) {
        return teamRepository.findAll().stream()
                .filter(team -> team.getName().equals(name) && team.getCohort().getId().equals(cohort.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Team team = Team.builder()
                            .name(name)
                            .cohort(cohort)
                            .build();
                    return teamRepository.save(team);
                });
    }

    private Member createAdminIfNotExists() {
        return memberRepository.findByLoginId("admin")
                .orElseGet(() -> {
                    Member admin = Member.builder()
                            .loginId("admin")
                            .passwordHash(passwordEncoder.encode("admin1234"))
                            .name("관리자")
                            .role(MemberRole.ADMIN)
                            .status(MemberStatus.ACTIVE)
                            .build();
                    return memberRepository.save(admin);
                });
    }

    private void createCohortMemberIfNotExists(Member member, Cohort cohort, Part part, Team team) {
        cohortMemberRepository.findByMemberIdAndCohortId(member.getId(), cohort.getId())
                .orElseGet(() -> {
                    CohortMember cohortMember = CohortMember.builder()
                            .member(member)
                            .cohort(cohort)
                            .part(part)
                            .team(team)
                            .build();
                    return cohortMemberRepository.save(cohortMember);
                });
    }
}
