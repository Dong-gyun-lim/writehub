package com.writehub.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchCondition {

    private String keyword; //제목 또는 내용 검색
    private String tag; //태그 검색
}
