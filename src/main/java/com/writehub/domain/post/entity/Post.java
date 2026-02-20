package com.writehub.domain.post.entity;

import com.writehub.domain.member.entity.Member;
import com.writehub.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility;

    //== 생성 메서드 ==//
    public static Post createPost(Member author, String title, String content, Visibility visibility) {
        Post post = new Post();
        post.author = author;
        post.title = title;
        post.content = content;
        post.visibility = visibility;
        post.viewCount = 0;
        return post;
    }

    //== 비즈니스 로직 ==//
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(String title, String content, Visibility visibility) {
        this.title = title;
        this.content = content;
        this.visibility = visibility;
    }

}
