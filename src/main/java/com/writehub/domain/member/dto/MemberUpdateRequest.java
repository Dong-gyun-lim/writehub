package com.writehub.domain.member.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

    @Size(max = 30, message = "닉네임은 30자를 초과할 수 없습니다")
    private String nickname;

    @Size(max = 255)
    private String bio;


}
