package com.writehub.domain.member.dto;

import com.writehub.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberDetailResponse {

    private final Long memberId;
    private final String username;
    private final String bio;
    private final long followerCount;
    private final long followingCount;
    private final long postCount;
    private final long subscriberCount;

    // Repository에서 count 조회해서 넣어줘야 함
    public MemberDetailResponse(Member member,
                                long followerCount,
                                long followingCount,
                                long postCount,
                                long subscriberCount) {
        this.memberId = member.getId();
        this.username = member.getUsername();
        this.bio = member.getBio();
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.postCount = postCount;
        this.subscriberCount = subscriberCount;
    }
}
