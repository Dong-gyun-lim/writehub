package com.writehub.domain.member.dto;

import com.writehub.domain.member.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberResponse {

    private final Long memberId;
    private final String email;
    private final String username;
    private final String bio;
    private final String nickname;
    private final LocalDateTime createdAt;

    // 엔티티 → DTO 변환 생성자
    public MemberResponse(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.username = member.getUsername();
        this.bio = member.getBio();
        this.nickname = member.getNickname();
        this.createdAt = member.getCreatedAt();
    }
}
