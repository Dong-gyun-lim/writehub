package com.writehub.domain.member.service;

import com.writehub.domain.follow.repository.FollowRepository;
import com.writehub.domain.member.dto.*;
import com.writehub.domain.member.entity.Member;
import com.writehub.domain.member.repository.MemberRepository;
import com.writehub.domain.post.repository.PostRepository;
import com.writehub.domain.subscription.repository.SubscriptionRepository;
import com.writehub.global.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 회원가입
     */
    @Transactional
    public MemberResponse singup(SignupRequest request) {
        // 1. 이메일 중복 체크 todo 전역 예외 처리 만들 때 커스텀 예외처리 만들어주기
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다");
        }

        // 2. 비밀번호 암호화(BCrypt로 암호화)
        String encodedPassword = PasswordEncoder.encode(request.getPassword());

        // 3. Member 생성
        Member member = Member.createMember(
                request.getEmail(),
                encodedPassword,
                request.getUsername(),
                request.getBio()
        );

        // 4. Member 저장
        memberRepository.save(member);

        // 5. DTO 변환 후 반환
        return new MemberResponse(member);
    }

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpSession session) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다"));

        // 2. 비밀번호 검증
        if (!PasswordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 3. 세션에 회원 ID 저장
        session.setAttribute("memberId", member.getId());

        //4. 응답 반환
        return new LoginResponse(member.getId(), member.getUsername());
    }

    /**
     * 로그아웃
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /**
     * 내 정보 조회
     */
    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));
        return new MemberResponse(member);
    }

    /**
     * 특정 회원 프로필 조회 (통계 정보 포함)
     */
    public MemberDetailResponse getMemberDetail(Long memberId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));

        // 2. 통계 정보 조회
        long followerCount = followRepository.countByFollowingId(memberId);
        long followingCount = followRepository.countByFollowerId(memberId);
        long postCount = postRepository.countByAuthorId(memberId);
        long subscriberCount = subscriptionRepository.countByCreatorId(memberId);

        // 3. DTO 생성 후 반환
        return new MemberDetailResponse(
                member,
                followerCount,
                followingCount,
                postCount,
                subscriberCount
        );
    }

    /**
     * 전체 회원 목록 조회(페이징)
     */
    public Page<MemberResponse> getMemberList(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(MemberResponse::new);
    }

}
