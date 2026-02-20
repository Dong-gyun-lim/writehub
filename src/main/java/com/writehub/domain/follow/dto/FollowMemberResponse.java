package com.writehub.domain.follow.dto;

import com.writehub.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class FollowMemberResponse {

    private final Long memberId;
    private final String username;
    private final String bio;

    // Member 엔티티 -> DTO 변환
    public FollowMemberResponse(Member member) {
        this.memberId=member.getId();
        this.username=member.getUsername();
        this.bio=member.getBio();
    }
}
