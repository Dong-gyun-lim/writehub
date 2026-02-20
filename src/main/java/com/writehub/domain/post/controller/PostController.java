package com.writehub.domain.post.controller;

import com.writehub.domain.post.dto.PostCreateRequest;
import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostResponse;
import com.writehub.domain.post.dto.PostUpdateRequest;
import com.writehub.domain.post.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            HttpSession session) {

        // 세션에서 작성자 ID 추출
        Long authorId = (Long) session.getAttribute("memberId");

        if (authorId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        PostResponse response = postService.createPost(authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<PostListResponse>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListResponse> response = postService.getPosts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId, HttpSession session) {
        // 세션에서 viewerId 추출 (로그인 안 했으면 null)
        Long viewerId = (Long) session.getAttribute("memberId");

        PostResponse response = postService.getPost(postId, viewerId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request, HttpSession session) {

        // 세션에서 작성자 ID 추출
        Long authorId = (Long) session.getAttribute("memberId");
        if (authorId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        PostResponse response = postService.updatePost(postId, authorId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId, HttpSession session) {

        // 세션에서 작성자 ID 추출
        Long authorId = (Long) session.getAttribute("memberId");
        if (authorId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }
        postService.deletePost(authorId, postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 회원의 게시글 목록 조회
     */
    @GetMapping("/members/{memberId}/posts")
    public ResponseEntity<Page<PostListResponse>> getPostsByAuthor(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostListResponse> response = postService.getPostsByAuthor(memberId, pageable);
        return ResponseEntity.ok(response);
    }
}
