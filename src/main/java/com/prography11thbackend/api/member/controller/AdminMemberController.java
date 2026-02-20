package com.prography11thbackend.api.member.controller;

import com.prography11thbackend.api.member.dto.MemberRegisterRequest;
import com.prography11thbackend.api.member.dto.MemberResponse;
import com.prography11thbackend.api.member.dto.MemberUpdateRequest;
import com.prography11thbackend.domain.member.entity.Member;
import com.prography11thbackend.domain.member.service.MemberService;
import com.prography11thbackend.global.exception.BusinessException;
import com.prography11thbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(@RequestBody MemberRegisterRequest request) {
        Member member = memberService.register(
                request.loginId(),
                request.password(),
                request.name(),
                request.cohortId(),
                request.part(),
                request.teamId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> members = memberService.getAllMembers().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        Member member = memberService.getMember(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateMember(id, request.name());
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdrawMember(@PathVariable Long id) {
        memberService.withdraw(id);
        return ResponseEntity.noContent().build();
    }
}
