package com.writehub.domain.member.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final Long memberId;
    private final String username;
    private final String message;

    public LoginResponse(Long memberId, String username) {
        this.memberId = memberId;
        this.username = username;
        this.message = "로그인 성공";
    }

}
