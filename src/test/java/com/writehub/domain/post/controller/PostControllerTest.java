package com.writehub.domain.post.controller;

import com.writehub.domain.member.entity.Member;
import com.writehub.domain.member.repository.MemberRepository;
import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostSearchCondition;
import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.PostTag;
import com.writehub.domain.post.entity.Visibility;
import com.writehub.domain.post.repository.PostRepository;
import com.writehub.domain.post.repository.PostTagRepository;
import com.writehub.domain.tag.entity.Tag;
import com.writehub.domain.tag.repository.TagRepository;
import com.writehub.global.config.QuerydslConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QuerydslConfig.class)
class PostControllerTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        member = memberRepository.save(
                Member.createMember("test@test.com", "password123", "테스터", null)
        );

        // 게시글 1: 제목에 Spring 포함
        Post post1 = postRepository.save(
                Post.createPost(member, "Spring Boot 입문", "내용입니다", Visibility.PUBLIC)
        );

        // 게시글 2: 내용에 JPA 포함
        Post post2 = postRepository.save(
                Post.createPost(member, "백엔드 개발", "JPA를 활용한 개발", Visibility.PUBLIC)
        );

        // 게시글 3: 태그에 Docker 포함
        Post post3 = postRepository.save(
                Post.createPost(member, "인프라 공부", "내용입니다", Visibility.PUBLIC)
        );

        // post3에 Docker 태그 추가
        Tag dockerTag = tagRepository.save(Tag.createTag("Docker"));
        postTagRepository.save(PostTag.createPostTag(post3, dockerTag));
    }

    @Test
    @DisplayName("키워드로 제목 검색")
    void searchByTitleKeyword() {
        PostSearchCondition condition = new PostSearchCondition("Spring", null);
        PageRequest pageable = PageRequest.of(0, 10);

        Page<PostListResponse> result = postRepository.searchPosts(condition, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("조건 없으면 전체 조회")
    void searchWithNoCondition() {
        PostSearchCondition condition = new PostSearchCondition(null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<PostListResponse> result = postRepository.searchPosts(condition, pageable);

        assertThat(result.getContent()).hasSize(3);
    }
}