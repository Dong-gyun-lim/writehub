package com.writehub.domain.post.repository;

import com.writehub.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    // 특정 게시글의 모든 태그
    List<PostTag> findByPostId(Long postId);

    // 특정 게시글의 태그 삭제 (게시글 수정 시)
    @Transactional
    void deleteByPostId(Long postId);
}
