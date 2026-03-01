package com.writehub.global.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends CustomException {
    public DuplicateException(String message) {
        super(message, HttpStatus.CONFLICT); //409 이미 존재하는 걸 또 만들 때 사용
    }
}
