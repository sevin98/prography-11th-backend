package com.prography11thbackend.domain.auth.service;

import com.prography11thbackend.domain.member.entity.Member;

public interface AuthService {

    Member login(String loginId, String password);
}
