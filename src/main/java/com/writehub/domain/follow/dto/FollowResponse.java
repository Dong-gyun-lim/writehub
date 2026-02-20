package com.writehub.domain.follow.dto;

import lombok.Getter;

@Getter
public class FollowResponse {

    private final Long followId;
    private final Long followerId;
    private final Long followingId;
    private final String message;

    // 팔로우 성공 시
    public FollowResponse(Long followId, Long followerId, Long followingId) {
        this.followId=followId;
        this.followerId=followerId;
        this.followingId=followingId;
        this.message = "팔로우 성공";
    }

    // 언팔로우 선공 시 (followId 없음)
    public FollowResponse(Long followerId, Long followingId) {
        this.followId = null;
        this.followerId=followerId;
        this.followingId=followingId;
        this.message = "언팔로우 성공";
    }
}
