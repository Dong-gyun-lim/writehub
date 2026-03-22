package com.writehub.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostSearchCondition;
import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.QPostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.writehub.domain.post.entity.QPost.post;
import static com.writehub.domain.post.entity.QPostTag.postTag;
import static com.writehub.domain.tag.entity.QTag.tag;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListResponse> searchPosts(PostSearchCondition condition, Pageable pageable) {
        //1. id만 페이징 조회
        List<Long> postIds = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        keywordContains(condition.getKeyword()),
                        tagContains(condition.getTag())
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if(postIds.isEmpty()){
            return Page.empty(pageable);
        }

        //2. fetch join으로 조회
        List<Post> posts = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .leftJoin(post.postTags, postTag).fetchJoin()
                .leftJoin(postTag.tag, tag).fetchJoin()
                .where(post.id.in(postIds))
                .orderBy(post.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        keywordContains(condition.getKeyword()),
                        tagContains(condition.getTag())
                );

        List<PostListResponse> content = posts.stream()
                .map(p -> new PostListResponse(p, p.getPostTags().stream()
                        .map(pt -> pt.getTag().getName())
                        .toList()))
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Post> findPostsWithPaging(Pageable pageable) {
        //1. id만 페이징으로 조회
        List<Long> postIds = queryFactory
                .select(post.id)
                .from(post)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        //2. 해당 id로 fetch join
        List<Post> posts = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .leftJoin(post.postTags, postTag).fetchJoin()
                .leftJoin(postTag.tag, tag).fetchJoin()
                .where(post.id.in(postIds))
                .fetch();

        //count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post);

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Post> findPostsByAuthorWithPaging(Long authorId, Pageable pageable) {
        //1. ID조회
        List<Long> postIds = queryFactory
                .select(post.id)
                .from(post)
                .where(post.author.id.eq(authorId))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }

        //2. 해당 id로 fetch join
        List<Post> posts = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .leftJoin(post.postTags, postTag).fetchJoin()
                .leftJoin(postTag.tag, tag).fetchJoin()
                .where(post.id.in(postIds))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .where(post.author.id.eq(authorId))
                .from(post);

        return PageableExecutionUtils.getPage(posts, pageable, countQuery::fetchOne);
    }

    //제목 또는 내용에 키워드 포함 여부
    private BooleanExpression keywordContains(String keyword) {
        if(!StringUtils.hasText(keyword)) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    //태그 이름으로 검색 (서브쿼리)
    private BooleanExpression tagContains(String tag) {
        if (!StringUtils.hasText(tag)) return null;

        QPostTag postTagSub = new QPostTag("postTagSub");
        com.writehub.domain.tag.entity.QTag tagSub = new com.writehub.domain.tag.entity.QTag("tagSub");

        return post.id.in(
                JPAExpressions
                        .select(postTagSub.post.id)
                        .from(postTagSub)
                        .join(postTagSub.tag, tagSub)
                        .where(tagSub.name.containsIgnoreCase(tag))
        );
    }
}
