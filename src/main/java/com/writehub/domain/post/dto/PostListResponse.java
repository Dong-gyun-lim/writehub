package com.writehub.domain.post.dto;

import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.Visibility;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostListResponse {
    /**
     * content(전체 내용), updatedAt(수정일) 제외
     */

    private final Long postId;
    private final String title;
    private final Long authorId;
    private final String authorName;
    private final int viewCount;
    private final Visibility visibility;
    private final List<String> tags;
    private final LocalDateTime createdAt;

    //엔티티 + 태그 리스트 -> DTO 변환
    public PostListResponse(Post post, List<String> tags) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getUsername();
        this.viewCount = post.getViewCount();
        this.visibility = post.getVisibility();
        this.tags = tags;
        this.createdAt = post.getCreatedAt();
    }
}
