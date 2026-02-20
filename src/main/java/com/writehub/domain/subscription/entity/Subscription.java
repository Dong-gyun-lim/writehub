package com.writehub.domain.subscription.entity;

import com.writehub.domain.member.entity.Member;
import com.writehub.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subscriber_id", "creator_id"})
})
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Member subscriber; //구독자(구독하는 사람)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator; //작가(구독받는 사람)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionType subscriptionType;

    public static Subscription createSubscription(Member subscriber, Member creator) {
        Subscription subscription = new Subscription();
        subscription.subscriber = subscriber;
        subscription.creator = creator;
        subscription.subscriptionType = SubscriptionType.FREE;
        return subscription;
    }

}
