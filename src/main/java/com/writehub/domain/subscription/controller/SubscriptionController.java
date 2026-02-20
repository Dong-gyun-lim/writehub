package com.writehub.domain.subscription.controller;

import com.writehub.domain.subscription.dto.SubscriptionMemberResponse;
import com.writehub.domain.subscription.dto.SubscriptionResponse;
import com.writehub.domain.subscription.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 구독
     */
    @PostMapping("/members/{creatorId}/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @PathVariable Long creatorId, HttpSession session) {
        // 1.세션에서 구독자 ID 추출
        Long subscriberId = (Long) session.getAttribute("memberId");

        if (subscriberId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        SubscriptionResponse response = subscriptionService.subscribe(creatorId, subscriberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 구독 취소
     */
    @DeleteMapping("/members/{creatorId}/subscribe")
    public ResponseEntity<SubscriptionResponse> unsubscribe(
            @PathVariable Long creatorId, HttpSession session) {
        // 세션에서 구독자 ID 추출
        Long subscriberId = (Long) session.getAttribute("memberId");

        if (subscriberId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }
        SubscriptionResponse response = subscriptionService.unSubscribe(subscriberId, creatorId);
        return ResponseEntity.ok(response);
    }

    /**
     * 구독 목록 조회 (내가 구독한 크리에이터들)
     */
    @GetMapping("/members/{memberId}/subscriptions")
    public ResponseEntity<Page<SubscriptionMemberResponse>> getSubscriptionList(
            @PathVariable Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SubscriptionMemberResponse> response = subscriptionService.getSubscriptionList(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 구독자 목록 조회 (나를 구독한 사람들)
     */
    @GetMapping("/members/{creatorId}/subscribers")
    public ResponseEntity<Page<SubscriptionMemberResponse>> getSubscriberList(
            @PathVariable Long creatorId, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SubscriptionMemberResponse> response = subscriptionService.getSubscriberList(creatorId, pageable);
        return ResponseEntity.ok(response);
    }
}
