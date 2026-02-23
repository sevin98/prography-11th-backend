package com.prography11thbackend.domain.cohort.service;

import com.prography11thbackend.domain.cohort.entity.Cohort;
import com.prography11thbackend.domain.cohort.entity.CohortMember;
import com.prography11thbackend.domain.cohort.entity.Part;
import com.prography11thbackend.domain.cohort.entity.Team;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.cohort.repository.CohortRepository;
import com.prography11thbackend.domain.cohort.repository.TeamRepository;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.repository.MemberRepository;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CohortServiceImpl implements CohortService {

    private final CohortRepository cohortRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cohort> getAllCohorts() {
        return cohortRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cohort> getCohortById(Long id) {
        return cohortRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cohort> getCohortByNumber(Integer number) {
        return cohortRepository.findByNumber(number);
    }

    @Override
    public CohortMember createCohortMember(Long memberId, Long cohortId, Long partId, Long teamId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));

        Part partEnum = null;
        if (partId != null) {
            // partId는 Part enum의 ordinal() + 6과 매핑 (6=SERVER, 7=WEB, 8=iOS, 9=ANDROID, 10=DESIGN)
            Part[] parts = Part.values();
            if (partId >= 6 && partId < 6 + parts.length) {
                partEnum = parts[(int)(partId - 6)];
            } else {
                throw new BusinessException(ErrorCode.PART_NOT_FOUND);
            }
        } else {
            throw new BusinessException(ErrorCode.PART_NOT_FOUND);
        }

        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        }

        CohortMember cohortMember = CohortMember.builder()
                .member(member)
                .cohort(cohort)
                .part(partEnum)
                .team(team)
                .build();

        return cohortMemberRepository.save(cohortMember);
    }
}
