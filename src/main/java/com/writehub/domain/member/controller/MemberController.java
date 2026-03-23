package com.writehub.domain.member.controller;

import com.writehub.domain.member.dto.*;
import com.writehub.domain.member.service.MemberService;
import com.writehub.global.common.ApiResponse;
import com.writehub.global.common.LoginMember;
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
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.singup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입 성공", response));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = memberService.login(request, session);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        memberService.logout(session);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/members/me")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getMyInfo(@LoginMember Long memberId) {

        // 회원 정보 조회
        MemberDetailResponse response = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회 성공", response));
    }

    /**
     * 특정 회원 프로필 조회
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getMemberDetail(@PathVariable Long memberId) {
        MemberDetailResponse response = memberService.getMemberDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원 프로필 조회 성공", response));
    }

    /**
     * 전체 회원 목록 조회
     */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MemberResponse> response = memberService.getMemberList(pageable);
        return ResponseEntity.ok(ApiResponse.success("전체 회원 목록 조회 성공", response));
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/members/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @LoginMember Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateProfile(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", response));
    }

}