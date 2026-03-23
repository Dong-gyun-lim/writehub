package com.writehub.domain.post.service;

import com.writehub.domain.member.entity.Member;
import com.writehub.domain.post.entity.Post;
import com.writehub.domain.post.entity.Visibility;
import com.writehub.domain.post.repository.PostRepository;
import com.writehub.domain.post.repository.PostTagRepository;
import com.writehub.domain.subscription.repository.SubscriptionRepository;
import com.writehub.global.exception.ForbiddenException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.writehub.domain.post.entity.Visibility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    PostTagRepository postTagRepository;

    @Mock
    SubscriptionRepository subscriptionRepository;

    @InjectMocks
    PostService postService;

    @Test
    void 구독자_전용_게시글_비구독자_조회() {
        //given
        Member author = Member.createMember("admin1234@naver.com", "admin1234", "어드민", "안녕하세요 어드민 입니다");
        ReflectionTestUtils.setField(author, "id", 1L);
        Post post = Post.createPost(author, "제목", "내용", SUBSCRIBER_ONLY);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(subscriptionRepository.existsBySubscriberIdAndCreatorId(2L, author.getId()))
                .thenReturn(false);

        //when & then
        Assertions.assertThatThrownBy(() -> postService.getPost(1L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("구독자만 볼 수 있는 게시글입니다");

    }

}