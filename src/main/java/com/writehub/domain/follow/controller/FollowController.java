package com.writehub.domain.follow.controller;

import com.writehub.domain.follow.dto.FollowMemberResponse;
import com.writehub.domain.follow.dto.FollowResponse;
import com.writehub.domain.follow.service.FollowService;
import com.writehub.global.common.ApiResponse;
import com.writehub.global.common.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우
     */
    @PostMapping("/members/{followingId}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> follow(
            @PathVariable() Long followingId, @LoginMember Long followerId) {

        FollowResponse response = followService.follow(followerId, followingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("팔로우 성공", response));
    }

    /**
     * 언팔로우
     */
    @DeleteMapping("/members/{followingId}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> unfollow(
            @PathVariable Long followingId, @LoginMember Long followerId) {

        FollowResponse response = followService.unfollow(followerId, followingId);
        return ResponseEntity.ok(ApiResponse.success("언팔로우 성공", response));
    }

    /**
     * 팔로잉 목록 조회 (내가 팔로우한 사람들) - 프론트 구현 X
     */
    @GetMapping("/members/{memberId}/following")
    public ResponseEntity<ApiResponse<Page<FollowMemberResponse>>> getFollowingList(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<FollowMemberResponse> response = followService.getFollowingList(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success("팔로잉 목록 조회 성공", response));
    }

    /**
     * 팔로워 목록 조회 (나를 팔로우한 사람들) - 프론트 구현 X
     */
    @GetMapping("/members/{memberId}/followers")
    public ResponseEntity<ApiResponse<Page<FollowMemberResponse>>> getFollowerList(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FollowMemberResponse> response = followService.getFollowerList(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success("팔로워 목록 조회 성공", response));
    }
}
