package com.writehub.domain.tag.repository;

import com.writehub.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름으로 찾기
    Optional<Tag> findByName(String name);

    // 태그 존재 여부
    boolean existsByName(String name);
}
