package com.writehub.domain.post.service;

import com.writehub.domain.member.entity.Member;
import com.writehub.domain.member.repository.MemberRepository;
import com.writehub.domain.post.dto.PostCreateRequest;
import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostResponse;
import com.writehub.domain.post.dto.PostUpdateRequest;
import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.PostTag;
import com.writehub.domain.post.entity.Visibility;
import com.writehub.domain.post.repository.PostRepository;
import com.writehub.domain.post.repository.PostTagRepository;
import com.writehub.domain.subscription.repository.SubscriptionRepository;
import com.writehub.domain.tag.entity.Tag;
import com.writehub.domain.tag.repository.TagRepository;
import com.writehub.global.exception.ForbiddenException;
import com.writehub.global.exception.NotFoundException;
import com.writehub.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 게시글 작성
     */
    @Transactional
    public PostResponse createPost(Long authorId, PostCreateRequest request) {
        // 1. 작성자 조회
        Member author = memberRepository.findById(authorId).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다"));

        // 2. 게시글 생성
        Post post = Post.createPost(
                author,
                request.getTitle(),
                request.getContent(),
                request.getVisibility()
        );
        postRepository.save(post);

        // 3. 태그 처리
        List<String> tagNames = saveTags(post, request.getTags());

        // 4. DTO 반환
        return new PostResponse(post, tagNames);
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    public Page<PostListResponse> getPosts(Pageable pageable) {
        // 1. 게시글 목록 조회(페이징)
        Page<Post> posts = postRepository.findAll(pageable);

        // 2. Post -> PostListResponse(DTO) 변환
        return posts.map(post -> {
            //각 게시글의 태그 조회
            List<String> tags = postTagRepository.findByPostId(post.getId()).stream()
                    .map(postTag -> postTag.getTag().getName())
                    .toList();

            return new PostListResponse(post, tags);
        });
    }

    /**
     * 게시글 상세 조회 (조회수 증가)
     */
    @Transactional
    public PostResponse getPost(Long postId, Long viewerId) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        // 2. 공개 범위 체크
        if (post.getVisibility() == Visibility.SUBSCRIBER_ONLY) {
            // 로그인 안했으면
            if(viewerId == null) {
                throw new UnauthorizedException("로그인이 필요합니다");
            }

            // 작성자 본인이 아니고, 구독자도 아니면
            if(!post.getAuthor().getId().equals(viewerId) &&
                    !subscriptionRepository.existsBySubscriberIdAndCreatorId(viewerId, post.getAuthor().getId())
            ) {
                throw new ForbiddenException("구독자만 볼 수 있는 게시글입니다");
            }
        }

        // 3. 조회수 증가
        if(viewerId == null || !post.getAuthor().getId().equals(viewerId)) {
            post.increaseViewCount();
        }

        // 4. 태그 조회
        List<String> tags = postTagRepository.findByPostId(postId).stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();

        // 5. DTO 변환
        return new PostResponse(post, tags);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long postId, Long authorId, PostUpdateRequest request) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        // 2. 작성자 확인
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new ForbiddenException("본인의 게시글만 수정할 수 있습니다");
        }

        // 3. 게시글 내용 수정
        post.update(request.getTitle(), request.getContent(), request.getVisibility());

        // 4. 태그가 변경 되었는지 확인
        List<String> currentTags = postTagRepository.findByPostId(postId).stream()
                .map(postTag -> postTag.getTag().getName())
                .toList();

        // 5. 태그가 변경된 경우만 처리
        if (request.getTags() != null) {
            List<String> newTags = request.getTags();

            Set<String> currentTagSet = new HashSet<>(currentTags);
            Set<String> newTagSet = new HashSet<>(newTags);

            if (!currentTagSet.equals(newTagSet)) {
                postTagRepository.deleteByPostId(postId);
                postTagRepository.flush();
                saveTags(post, newTags);
                return new PostResponse(post, newTags);
            }
        }

        return new PostResponse(post, currentTags);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long authorId) {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다"));

        // 2. 작성자 확인
        if(!post.getAuthor().getId().equals(authorId)) {
            throw new ForbiddenException("본인의 게시글만 삭제할 수 있습니다");
        }

        // 3. PostTag 먼저 삭제
        postTagRepository.deleteByPostId(postId);

        // 4. 게시글 삭제
        postRepository.delete(post);
    }

    /**
     * 특정 회원의 게시글 목록 조회 (페이징)
     */
    public Page<PostListResponse> getPostsByAuthor(Long authorId, Pageable pageable) {
        // 1. 해당 회원의 게시글 목록 조회
        Page<Post> posts = postRepository.findByAuthorId(authorId, pageable);

        // 2. Post -> PostListResponse(DTO) 변환
        return posts.map(post -> {
            List<String> tags = postTagRepository.findByPostId(post.getId()).stream()
                    .map(postTag -> postTag.getTag().getName())
                    .toList();

            return new PostListResponse(post, tags);
        });
    }

    /**
     * 태그 저장 (private 헬퍼 메서드)
     */
    private List<String> saveTags(Post post, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        for (String tagName : tagNames) {
            //태그가 없으면 생성, 있으면 조회
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.createTag(tagName);
                        return tagRepository.save(newTag);
                    });

            //PostTag 중간 테이블 저장
            PostTag postTag = PostTag.createPostTag(post, tag);
            postTagRepository.save(postTag);
        }
        return tagNames;
    }
}
