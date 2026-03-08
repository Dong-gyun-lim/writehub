package com.writehub.domain.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.writehub.domain.post.dto.PostListResponse;
import com.writehub.domain.post.dto.PostSearchCondition;
import com.writehub.domain.post.entity.Post;
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
        List<Post> posts = queryFactory
                .selectFrom(post)
                .where(
                        keywordContains(condition.getKeyword()),
                        tagContains(condition.getTag())
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        keywordContains(condition.getKeyword()),
                        tagContains(condition.getTag())
                );

        List<PostListResponse> content = posts.stream()
                .map(p -> new PostListResponse(p, getTagNames(p.getId())))
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    //제목 또는 내용에 키워드 포함 여부
    private BooleanExpression keywordContains(String keyword) {
        if(!StringUtils.hasText(keyword)) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    //태그 이름으로 검색 (서브쿼리)
    private BooleanExpression tagContains(String tag) {
        if(!StringUtils.hasText(tag)) return null;
        return post.id.in(
                JPAExpressions
                        .select(postTag.post.id)
                        .from(postTag)
                        .where(postTag.tag.name.containsIgnoreCase(tag))
        );
    }

    //태그 이름 목록 추출
    private List<String> getTagNames(Long postId) {
        return queryFactory
                .select(tag.name)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch();
    }
}
