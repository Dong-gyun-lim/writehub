package com.writehub.domain.member.repository;

import com.writehub.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    //이메일 중복 체크
    boolean existsByEmail(String email);
}
