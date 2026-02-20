package com.writehub.domain.follow.repository;

import com.writehub.domain.follow.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로우 관계 존재 여부
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 팔로우 관계 찾기
    Optional<Follow> findByFollowerIdANDFollowingId(Long followerId, Long followingId);

    // 사용자가 팔로우한 작가 목록
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    // 작가(나)를 팔로우한 사용자 목록
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    //팔로워 수
    long countByFollowingId(Long followingId);

    // 팔로잉 수
    long countByFollowerId(Long followerId);
}
