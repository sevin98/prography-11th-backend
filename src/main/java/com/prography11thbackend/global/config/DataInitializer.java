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
import com.prography11thbackend.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepositService depositService;
    private final MemberService memberService;
    private final Random random = new Random();

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

        // 랜덤 유저 16명 생성
        createRandomUsers(cohort11, teamA, teamB, teamC);

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
                            .phone("010-0000-0000")
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

    private void createRandomUsers(Cohort cohort11, Team teamA, Team teamB, Team teamC) {
        // 기존 유저 수 확인
        long existingUserCount = memberRepository.count() - 1; // 관리자 제외
        
        if (existingUserCount >= 16) {
            log.info("이미 16명 이상의 유저가 존재합니다. 랜덤 유저 생성을 건너뜁니다.");
            return;
        }

        int usersToCreate = 16 - (int) existingUserCount;
        log.info("랜덤 유저 {}명 생성 시작", usersToCreate);

        // 한글 성씨 리스트
        String[] surnames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "전", "홍"};
        
        // 한글 이름 중간 글자 리스트
        String[] middleNames = {"민", "지", "서", "현", "수", "준", "도", "예", "하", "윤", "채", "원", "시", "유", "나", "다", "라", "마", "바", "사"};
        
        // 한글 이름 마지막 글자 리스트
        String[] lastNames = {"수", "준", "현", "영", "우", "진", "희", "민", "은", "지", "원", "율", "아", "연", "서", "하", "예", "나", "다", "라"};

        // 파트 리스트
        Part[] parts = Part.values();
        
        // 팀 리스트
        Team[] teams = {teamA, teamB, teamC};

        for (int i = 0; i < usersToCreate; i++) {
            // 랜덤 한글 이름 생성
            String surname = surnames[random.nextInt(surnames.length)];
            String middleName = middleNames[random.nextInt(middleNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String name = surname + middleName + lastName;

            // 랜덤 loginId 생성 (user1, user2, ... 형식)
            String loginId = "user" + (existingUserCount + i + 1);
            
            // 중복 체크
            while (memberRepository.existsByLoginId(loginId)) {
                loginId = "user" + System.currentTimeMillis() + random.nextInt(1000);
            }

            // 랜덤 파트 선택
            Part part = parts[random.nextInt(parts.length)];
            
            // 랜덤 팀 선택
            Team team = teams[random.nextInt(teams.length)];

            // 회원 등록 (보증금은 자동으로 100,000원 설정됨)
            // phone은 null로 설정 (랜덤 유저는 전화번호 없음)
            // partId는 Part enum의 ordinal() + 6 (SERVER=6, WEB=7, iOS=8, ANDROID=9, DESIGN=10)
            Long partId = (long) (part.ordinal() + 6);
            memberService.register(loginId, "password1234", name, null, cohort11.getId(), partId, team.getId());
        }

        log.info("랜덤 유저 {}명 생성 완료", usersToCreate);
    }
}
