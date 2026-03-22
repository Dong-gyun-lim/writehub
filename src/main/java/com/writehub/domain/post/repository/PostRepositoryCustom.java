package com.writehub.domain.post.repository;

import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostSearchCondition;
import com.writehub.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PostRepositoryCustom{
    Page<PostListResponse> searchPosts(PostSearchCondition condition, Pageable pageable);

    Page<Post> findPostsWithPaging(Pageable pageable);

    Page<Post> findPostsByAuthorWithPaging(Long authorId, Pageable pageable);
}
