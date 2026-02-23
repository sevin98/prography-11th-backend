package com.prography11thbackend.api.member.controller;

import com.prography11thbackend.api.member.dto.MemberRegisterRequest;
import com.prography11thbackend.api.member.dto.MemberResponse;
import com.prography11thbackend.api.member.dto.MemberUpdateRequest;
import com.prography11thbackend.domain.cohort.repository.CohortMemberRepository;
import com.prography11thbackend.domain.deposit.service.DepositService;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.service.MemberService;
import com.prography11thbackend.global.common.ApiResponse;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@Validated
public class AdminMemberController {

    private static final Integer CURRENT_COHORT_NUMBER = 11; // 현재 기수 번호

    private final MemberService memberService;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        Member member = memberService.register(
                request.loginId(),
                request.password(),
                request.name(),
                request.phone(),
                request.cohortId(),
                request.partId(),
                request.teamId()
        );
        
        // 현재 기수의 CohortMember 조회
        var cohortMember = cohortMemberRepository.findByMemberIdAndCohortId(member.getId(), request.cohortId())
                .orElse(null);
        
        // 명세서에는 deposit이 없으므로 null로 설정
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MemberResponse.from(member, cohortMember, null)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.member.dto.MemberDashboardResponse>> getAllMembers(
            @RequestParam(required = false, defaultValue = "0") @Min(value = 0, message = "페이지는 0 이상이어야 합니다") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다") Integer size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) Integer generation,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status
    ) {
        // 모든 회원 조회
        List<com.prography11thbackend.domain.member.entity.Member> allMembers = memberService.getAllMembers();
        
        // DB 레벨 필터: status, searchType+searchValue
        List<com.prography11thbackend.domain.member.entity.Member> filteredMembers = allMembers.stream()
                .filter(member -> {
                    // status 필터
                    if (status != null && !status.isBlank()) {
                        if (!member.getStatus().name().equals(status)) {
                            return false;
                        }
                    }
                    
                    // searchType + searchValue 필터
                    if (searchType != null && searchValue != null && !searchValue.isBlank()) {
                        switch (searchType.toLowerCase()) {
                            case "name":
                                if (!member.getName().contains(searchValue)) {
                                    return false;
                                }
                                break;
                            case "loginid":
                                if (!member.getLoginId().contains(searchValue)) {
                                    return false;
                                }
                                break;
                            case "phone":
                                if (member.getPhone() == null || !member.getPhone().contains(searchValue)) {
                                    return false;
                                }
                                break;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 메모리 레벨 필터: generation, partName, teamName
        List<com.prography11thbackend.domain.member.entity.Member> finalMembers = filteredMembers.stream()
                .filter(member -> {
                    var cohortMember = cohortMemberRepository.findByMemberId(member.getId()).stream()
                            .filter(cm -> cm.getCohort().getNumber().equals(CURRENT_COHORT_NUMBER))
                            .findFirst()
                            .orElse(null);
                    
                    if (cohortMember == null) {
                        return generation == null && partName == null && teamName == null;
                    }
                    
                    // generation 필터
                    if (generation != null && !cohortMember.getCohort().getNumber().equals(generation)) {
                        return false;
                    }
                    
                    // partName 필터
                    if (partName != null && !partName.isBlank()) {
                        if (cohortMember.getPart() == null || !cohortMember.getPart().name().equals(partName)) {
                            return false;
                        }
                    }
                    
                    // teamName 필터
                    if (teamName != null && !teamName.isBlank()) {
                        if (cohortMember.getTeam() == null || !cohortMember.getTeam().getName().equals(teamName)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 페이징 처리
        int totalElements = finalMembers.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<com.prography11thbackend.domain.member.entity.Member> pagedMembers = 
                start < totalElements ? finalMembers.subList(start, end) : List.of();
        
        // MemberResponse 변환
        List<MemberResponse> memberResponses = pagedMembers.stream()
                .map(member -> {
                    var cohortMember = cohortMemberRepository.findByMemberId(member.getId()).stream()
                            .filter(cm -> cm.getCohort().getNumber().equals(CURRENT_COHORT_NUMBER))
                            .findFirst()
                            .orElse(null);
                    
                    // deposit 조회
                    Integer deposit = depositService.findDepositByMemberId(member.getId())
                            .map(com.prography11thbackend.domain.deposit.entity.Deposit::getBalance)
                            .orElse(null);
                    
                    return MemberResponse.from(member, cohortMember, deposit);
                })
                .collect(Collectors.toList());
        
        // deposit을 포함한 MemberResponse를 위해 별도 DTO 필요하지만, 일단 기존 구조 유지
        // TODO: MemberResponse에 deposit 필드 추가 필요
        
        com.prography11thbackend.api.member.dto.MemberDashboardResponse dashboardResponse = 
                new com.prography11thbackend.api.member.dto.MemberDashboardResponse(
                        memberResponses,
                        page,
                        size,
                        (long) totalElements,
                        totalPages
                );
        
        return ResponseEntity.ok(ApiResponse.success(dashboardResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(@PathVariable Long id) {
        Member member = memberService.getMember(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        
        var cohortMember = cohortMemberRepository.findByMemberId(id).stream()
                .filter(cm -> cm.getCohort().getNumber().equals(CURRENT_COHORT_NUMBER))
                .findFirst()
                .orElse(null);
        
        // 명세서에는 deposit이 없으므로 null로 설정
        return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(member, cohortMember, null)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateMember(id, request.name(), request.phone(), request.cohortId(), request.partId(), request.teamId());
        
        var cohortMember = cohortMemberRepository.findByMemberId(id).stream()
                .filter(cm -> cm.getCohort().getNumber().equals(CURRENT_COHORT_NUMBER))
                .findFirst()
                .orElse(null);
        
        // 명세서에는 deposit이 없으므로 null로 설정
        return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(member, cohortMember, null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<com.prography11thbackend.api.member.dto.MemberWithdrawResponse>> withdrawMember(@PathVariable Long id) {
        Member member = memberService.withdraw(id);
        return ResponseEntity.ok(ApiResponse.success(com.prography11thbackend.api.member.dto.MemberWithdrawResponse.from(member)));
    }
}
