package com.writehub.domain.post.dto;

import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.Visibility;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponse {

    private final Long postId;
    private final String title;
    private final String content;
    private final Long authorId;
    private final String authorName;
    private final int viewCount;
    private final Visibility visibility;
    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 엔티티 + 태그 리스트 -> DTO 변환
    public PostResponse(Post post, List<String> tags) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getUsername();
        this.viewCount = post.getViewCount();
        this.visibility = post.getVisibility();
        this.tags = tags;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
