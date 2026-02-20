package com.writehub.domain.follow.entity;

import com.writehub.domain.member.entity.Member;
import com.writehub.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower; //팔로우 하는 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private Member following; //팔로우 받는 사람

    //==생성 메서드==//
    public static Follow createFollow(Member follower, Member following){
        Follow follow=new Follow();
        follow.follower = follower;
        follow.following = following;
        return follow;
    }
}
