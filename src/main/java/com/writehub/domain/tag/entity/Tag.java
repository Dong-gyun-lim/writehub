package com.writehub.domain.tag.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    //== 생성 메서드 =//
    public static Tag createTag(String name) {
        Tag tag=new Tag();
        tag.name = name;
        return tag;
    }
}
