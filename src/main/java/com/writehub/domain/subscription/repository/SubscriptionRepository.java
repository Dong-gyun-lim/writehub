package com.writehub.domain.subscription.repository;

import com.writehub.domain.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 구독 관계 존재 여부
    boolean existsBySubscriberIdAndCreatorId(Long subscriberId, Long creatorId);

    // 구독 관계 찾기
    Optional<Subscription> findBySubscriberIdAndCreatorId(Long subscriberId, Long creatorId);

    // 내가 구독한 작가 목록
    Page<Subscription> findBySubscriberId(Long subscriberId, Pageable pageable);

    // 나를 구독한 구독자 목록
    Page<Subscription> findByCreatorId(Long creatorId, Pageable pageable);

    // 구독자 수
    long countByCreatorId(Long creatorId);

}
