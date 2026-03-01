package com.writehub.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String code; // "NOT_FOUND", "UNAUTHORIZED" 등
    private final String message; // "회원을 찾을 수 없습니다" 등
}
