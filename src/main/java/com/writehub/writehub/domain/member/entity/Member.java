package com.writehub.writehub.domain.member.entity;

import com.writehub.writehub.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String bio;

    //== 생성 메서드 ==//
    public static Member createMember(String email, String password, String username, String bio) {
        Member member=new Member();
        member.email=email;
        member.password=password;
        member.username=username;
        member.bio=bio;
        return member;
    }

    //== 비즈니스 로직 ==//
    public void updateProfile(String username, String bio) {
        this.username=username;
        this.bio=bio;
    }

    public void changePassword(String newPassword) {
        this.password=newPassword;
    }

}
