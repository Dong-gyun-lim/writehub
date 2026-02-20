package com.writehub.domain.post.repository;

import com.writehub.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 특정 회원의 게시글 목록
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    // 특정 회원의 게시글 수
    long countByAuthorId(Long authorId);
}
