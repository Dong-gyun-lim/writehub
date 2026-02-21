package com.writehub.domain.follow.service;

import com.writehub.domain.follow.dto.FollowMemberResponse;
import com.writehub.domain.follow.dto.FollowResponse;
import com.writehub.domain.follow.entity.Follow;
import com.writehub.domain.follow.repository.FollowRepository;
import com.writehub.domain.member.entity.Member;
import com.writehub.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    /**
     * 팔로우
     */
    @Transactional
    public FollowResponse follow(Long followerId, Long followingId) {
        // 1. 자기 자신 팔로우 방지
        if(followerId.equals(followingId)){
            throw new RuntimeException("자기 자신은 팔로워할 수 없습니다");
        }

        // 2. 회원 존재 확인
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));

        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));

        // 3. 중복 팔로우 방지
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("이미 팔로우 중입니다");
        }

        // 4. 팔로우 생성
        Follow follow = Follow.createFollow(follower, following);
        followRepository.save(follow);

        // 5. 응답 반환
        return new FollowResponse(follow.getId(), followerId, followingId);
    }

    /**
     * 언팔로우
     */
    @Transactional
    public FollowResponse unfollow(Long followerId, Long followingId) {
        // 1. 팔로우 관계 조회
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("팔로우 관계가 존재하지 않습니다"));

        // 2. 팔로우 삭제
        followRepository.delete(follow);

        // 3. 응답 반환
        return new FollowResponse(followerId, followingId);
    }

    /**
     * 팔로잉 목록 조회 (내가 팔로우한 사람들)
     */
    public Page<FollowMemberResponse> getFollowingList(Long memberId, Pageable pageable) {
        // 1. 팔로우 관계 조회
        Page<Follow> follows = followRepository.findByFollowerId(memberId, pageable);

        // 2. Follow -> FollowMemberResponse(DTO) 변환
        return follows.map(follow -> new FollowMemberResponse(follow.getFollowing()));
    }

    /**
     * 팔로워 목록 조회 (나를 팔로우한 사람들)
     */
    public Page<FollowMemberResponse> getFollowerList(Long memberId, Pageable pageable) {
        // 1. 팔로우 관계 조회
        Page<Follow> follows = followRepository.findByFollowingId(memberId, pageable);

        // 2. Follow -> FollowMemberResponse(DTO) 변환
        return follows.map(follow -> new FollowMemberResponse(follow.getFollower()));
    }


}
