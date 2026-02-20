package com.writehub.domain.follow.controller;

import com.writehub.domain.follow.dto.FollowMemberResponse;
import com.writehub.domain.follow.dto.FollowResponse;
import com.writehub.domain.follow.service.FollowService;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<FollowResponse> follow(
            @PathVariable() Long followingId, HttpSession session) {

        // 세션에서 팔로워 ID 추출
        Long followerId = (Long) session.getAttribute("memberId");

        if (followerId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        FollowResponse response = followService.follow(followerId, followingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 언팔로우
     */
    @DeleteMapping("/members/{followingId}/follow")
    public ResponseEntity<FollowResponse> unfollow(
            @PathVariable Long followingId, HttpSession session) {

        // 세션에서 팔로워 ID 추출
        Long followerId = (Long) session.getAttribute("memberId");

        if (followerId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        FollowResponse response = followService.unfollow(followerId, followingId);
        return ResponseEntity.ok(response);
    }

    /**
     * 팔로잉 목록 조회 (내가 팔로우한 사람들)
     */
    @GetMapping("/members/{memberId}/following")
    public ResponseEntity<Page<FollowMemberResponse>> getFollowingList(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<FollowMemberResponse> response = followService.getFollowingList(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 팔로워 목록 조회 (나를 팔로우한 사람들)
     */
    @GetMapping("/members/{memberId}/followers")
    public ResponseEntity<Page<FollowMemberResponse>> getFollowerList(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FollowMemberResponse> response = followService.getFollowerList(memberId, pageable);
        return ResponseEntity.ok(response);
    }
}
