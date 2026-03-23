package com.writehub.domain.post.controller;

import com.writehub.domain.post.dto.*;
import com.writehub.domain.post.service.PostService;
import com.writehub.global.common.ApiResponse;
import com.writehub.global.common.LoginMember;
import com.writehub.global.common.SessionConst;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 작성
     */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request, @LoginMember Long authorId) {

        PostResponse response = postService.createPost(authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("게시글 작성 성공", response));
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListResponse> response = postService.getPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", response));
    }

    /**
     * 게시글 상세 조회(비로그인 허용)
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable Long postId, HttpSession session) {

        Long viewerId = (Long) session.getAttribute(SessionConst.MEMBER_ID);

        PostResponse response = postService.getPost(postId, viewerId);
        return ResponseEntity.ok(ApiResponse.success("게시글 조회 성공", response));
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request,
            @LoginMember Long authorId) {

        PostResponse response = postService.updatePost(postId, authorId, request);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId, @LoginMember Long authorId) {

        postService.deletePost(postId, authorId);
        return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공", null));
    }

    /**
     * 특정 회원의 게시글 목록 조회
     */
    @GetMapping("/members/{memberId}/posts")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPostsByAuthor(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostListResponse> response = postService.getPostsByAuthor(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", response));
    }

    /**
     * 게시글 검색
     */
    @GetMapping("/posts/search")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PostSearchCondition condition = new PostSearchCondition(keyword, tag);
        Page<PostListResponse> response = postService.searchPosts(condition, pageable);

        return ResponseEntity.ok(ApiResponse.success("게시글 검색 성공", response));
    }
}
