package com.writehub.domain.post.repository;

import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom{
    Page<PostListResponse> searchPosts(PostSearchCondition condition, Pageable pageable);
}
