package com.prography11thbackend.api.member.dto;

import java.util.List;

public record MemberDashboardResponse(
        List<MemberResponse> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {
}
