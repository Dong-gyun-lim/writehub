package com.writehub.domain.member.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final Long memberId;
    private final String username;
    private final String message;
    private final String nickname;

    public LoginResponse(Long memberId, String username, String nickname) {
        this.memberId = memberId;
        this.username = username;
        this.nickname = nickname;
        this.message = "로그인 성공";
    }

}
