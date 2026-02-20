package com.writehub.domain.subscription.dto;

import lombok.Getter;

@Getter
public class SubscriptionResponse {

    private final Long subscriptionId;
    private final Long subscriberId;
    private final Long creatorId;
    private final String message;

    //구독 성공시
    public SubscriptionResponse(Long subscriptionId, Long subscriberId, Long creatorId) {
     this.subscriptionId = subscriptionId;
     this.subscriberId = subscriberId;
     this.creatorId = creatorId;
     this.message = "구독 성공";
    }

    // 구독 취소 시 (subscriptionId 없음)
    public SubscriptionResponse(Long subscriberId, Long creatorId) {
        this.subscriptionId = null;
        this.subscriberId = subscriberId;
        this.creatorId = creatorId;
        this.message = "구독 취소 성공";
    }

}
