package com.writehub.domain.member.controller;

import com.writehub.domain.member.dto.*;
import com.writehub.domain.member.service.MemberService;
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
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    @PostMapping("/members")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.singup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = memberService.login(request, session);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        memberService.logout(session);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/members/me")
    public ResponseEntity<MemberDetailResponse> getMyInfo(HttpSession session) {
        // 1. 세션에서 memberId 추출
        Long memberId = (Long) session.getAttribute("memberId");

        // 2. 로그인 체크
        if (memberId == null) {
            throw new RuntimeException("로그인이 필요합니다");
        }

        // 3. 회원 정보 조회
        MemberDetailResponse response = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원 프로필 조회
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberDetailResponse> getMemberDetail(@PathVariable Long memberId) {
        MemberDetailResponse response = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 회원 목록 조회
     */
    @GetMapping("/members")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        Page<MemberResponse> response = memberService.getMemberList(pageable);
        return ResponseEntity.ok(response);
    }
}
