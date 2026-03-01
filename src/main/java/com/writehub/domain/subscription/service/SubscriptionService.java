package com.writehub.domain.subscription.service;

import com.writehub.domain.member.entity.Member;
import com.writehub.domain.member.repository.MemberRepository;
import com.writehub.domain.subscription.dto.SubscriptionMemberResponse;
import com.writehub.domain.subscription.dto.SubscriptionResponse;
import com.writehub.domain.subscription.entity.Subscription;
import com.writehub.domain.subscription.repository.SubscriptionRepository;
import com.writehub.global.exception.BadRequestException;
import com.writehub.global.exception.DuplicateException;
import com.writehub.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    /**
     * 구독
     */
    @Transactional
    public SubscriptionResponse subscribe(Long subscriberId, Long creatorId) {
        // 1. 자기 자신 구독 방지
        if(subscriberId.equals(creatorId)){
            throw new BadRequestException("자기 자신은 구독할 수 없습니다");
        }

        // 2. 회원 존재 확인
        Member subscriber = memberRepository.findById(subscriberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다"));

        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다"));

        // 3. 중복 구독 방지
        if(subscriptionRepository.existsBySubscriberIdAndCreatorId(subscriberId,creatorId)){
            throw new DuplicateException("이미 구독 중입니다");
        }

        // 4. 구독 생성
        Subscription subscription = Subscription.createSubscription(subscriber, creator);
        subscriptionRepository.save(subscription);

        // 5. 응답 변환
        return new SubscriptionResponse(subscription.getId(), subscriberId, creatorId);
    }

    /**
     * 구독 취소
     */
    @Transactional
    public SubscriptionResponse unSubscribe(Long subscriberId, Long creatorId) {
        // 1. 구독 관계 조회
        Subscription subscription = subscriptionRepository.findBySubscriberIdAndCreatorId(subscriberId, creatorId)
                .orElseThrow(() -> new NotFoundException("구독 관계가 존재하지 않습니다"));

        // 2. 구독 삭제
        subscriptionRepository.delete(subscription);

        return new SubscriptionResponse(subscriberId, creatorId);
    }

    /**
     * 구독 목록 조회 (내가 구독한 크리에이터들)
     */
    public Page<SubscriptionMemberResponse> getSubscriptionList(Long subscriberId, Pageable pageable) {
        // 1. 구독관계 조회
        Page<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(subscriberId, pageable);

        // 2. Subscription -> SubscriptionMemberResponse(DTO) 변환
        return subscriptions
                .map(subscription ->
                        new SubscriptionMemberResponse(subscription.getCreator()) // 크리에이터
                );
    }

    /**
     * 구독자 목록 조회 (나를 구독한 사람들)
     */
    public Page<SubscriptionMemberResponse> getSubscriberList(Long creatorId, Pageable pageable) {
        // 1. 구독 관계 조회
        Page<Subscription> subscriber = subscriptionRepository.findByCreatorId(creatorId, pageable);

        // 2. subscriber → SubscriptionMemberResponse 변환
        return subscriber.map(subscription ->
                new SubscriptionMemberResponse(subscription.getSubscriber()) //구독자
        );
    }
}
