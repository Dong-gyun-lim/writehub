# WriteHub

창작자를 위한 구독 기반 기술 블로그 플랫폼

## 📋 프로젝트 개요

**개발 기간**: 2025.02.17 ~ 2025.02.21 (약 5일)  
**개발 인원**: 1인 (백엔드)

**기획 의도**:
- 포스타입 서비스에 관심을 가지고 비슷한 도메인의 프로젝트 진행
- Spring Boot + JPA를 활용한 실전 프로젝트 경험
- 연관관계 매핑, 페이징 처리, N+1 문제 등 실무 기술 적용

---

## 🎯 핵심 기능

### MVP (v1.0) - 완료
- ✅ 회원 가입/로그인 (세션 기반)
- ✅ 게시글 CRUD
- ✅ 게시글 공개 범위 설정 (전체/구독자)
- ✅ 팔로우/언팔로우
- ✅ 무료 구독/구독 취소
- ✅ 태그 기능
- ✅ 조회수 기능

### 향후 확장 계획 (v2.0)
- 🔜 Spring Security + JWT 인증
- 🔜 좋아요 기능
- 🔜 댓글 기능
- 🔜 PG 결제 연동 (유료 구독)
- 🔜 정산 시스템 (Spring Batch)

---

## 🛠 기술 스택

### Backend
- Java 21
- Spring Boot 3.5.10
- Spring Data JPA
- MySQL 8.0
- Gradle

### Authentication
- BCrypt 비밀번호 암호화
- 세션 기반 인증

---

## 📊 ERD

### 핵심 엔티티
- **Member** (회원)
- **Post** (게시글)
- **Follow** (팔로우)
- **Subscription** (구독)
- **Tag** (태그)
- **PostTag** (게시글-태그 중간 테이블)

### 연관관계
```
Member (1) ──< (N) Post
Member (1) ──< (N) Follow ──> (1) Member (자기참조)
Member (1) ──< (N) Subscription ──> (1) Member (자기참조)
Post (N) ──< (N) Tag (PostTag 중간 테이블)
```

### 주요 설계 결정
- **단방향 매핑**: Post → Member (양방향 매핑 복잡도 방지)
- **CASCADE**: DB 레벨 ON DELETE CASCADE 사용
- **BaseTimeEntity**: 생성일/수정일 자동 관리

---

## 📁 패키지 구조
```
com.writehub
├── domain
│   ├── member (완료)
│   ├── post (완료)
│   ├── follow (완료)
│   ├── subscription (완료)
│   └── tag (완료)
└── global
    ├── common/BaseTimeEntity.java
    ├── config/
    ├── exception/
    └── util/PasswordEncoder.java
```

---

## 🔗 API 명세

### 회원 (Member)

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members | 회원가입 | - |
| POST | /api/login | 로그인 | - |
| POST | /api/logout | 로그아웃 | O |
| GET | /api/members/me | 내 정보 조회 | O |
| GET | /api/members/{memberId} | 특정 회원 조회 | - |

### 게시글 (Post)

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts | 게시글 작성 | O |
| GET | /api/posts | 게시글 목록 | - |
| GET | /api/posts/{postId} | 게시글 상세 | - |
| PUT | /api/posts/{postId} | 게시글 수정 | O |
| DELETE | /api/posts/{postId} | 게시글 삭제 | O |
| GET | /api/members/{memberId}/posts | 특정 회원 게시글 | - |

### 팔로우 (Follow)

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members/{followingId}/follow | 팔로우 | O |
| DELETE | /api/members/{followingId}/follow | 언팔로우 | O |
| GET | /api/members/{memberId}/following | 팔로잉 목록 | - |
| GET | /api/members/{memberId}/followers | 팔로워 목록 | - |

### 구독 (Subscription)

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members/{creatorId}/subscribe | 구독 | O |
| DELETE | /api/members/{creatorId}/subscribe | 구독 취소 | O |
| GET | /api/members/{memberId}/subscriptions | 구독 목록 | - |
| GET | /api/members/{creatorId}/subscribers | 구독자 목록 | - |

---

## 💡 기술적 의사결정

### 1. 게시글 태그 수정 전략

**선택**: 전체 삭제 후 재생성 + 변경 감지

**이유**:
- 태그는 보통 3~5개로 적음 (성능 차이 미미)
- 코드 가독성과 유지보수성 우선
- 변경 감지로 불필요한 재생성 방지

**코드**:
```java
Set<String> currentTagSet = new HashSet<>(currentTags);
Set<String> newTagSet = new HashSet<>(newTags);

if (!currentTagSet.equals(newTagSet)) {
    postTagRepository.deleteByPostId(postId);
    saveTags(post, newTags);
}
```

---

### 2. 인증 방식: BCrypt + 세션

**선택**: BCrypt + 세션 기반 인증

**이유**:
- 빠른 MVP 구현 (Security 학습 시간 절약)
- BCrypt는 업계 표준 암호화
- 설정이 간단하고 안정적

**향후 개선**: Spring Security + JWT 전환

---

### 3. ResponseEntity 사용

**선택**: ResponseEntity로 HTTP 상태 코드 제어

**이유**:
- RESTful API 설계 (201 Created, 204 No Content)
- 응답 의도 명확
- 확장성

---

### 4. 게시글 목록 조회 N+1 문제

**선택**: 현재 상태 유지 (N+1 허용)

**이유**:
- 한 페이지당 10~20개만 조회 (N이 작음)
- 조기 최적화 방지
- 실제 병목 확인 전까지 단순한 코드 유지

**향후 개선**: 일괄 조회로 최적화

---

### 5. 프로필 조회 Count 쿼리

**선택**: 실시간 COUNT 쿼리

**이유**:
- 프로필 조회 빈도가 높지 않음
- 항상 최신 데이터 반영
- 코드 복잡도 최소화

---

### 6. 조회수 증가 방식

**선택**: 조회 즉시 증가

**이유**:
- 구현 간단
- 실시간 반영
- 블로그 조회수는 대략적인 수치면 충분

**향후 개선**: Redis + 배치 처리

---

### 7. 공개 범위: 구독 기반

**선택**: 구독자 + 작성자 본인만 조회 가능

**이유**:
- 유료 블로그 플랫폼 특성
- 팔로우는 알림용, 구독은 콘텐츠 접근 권한

---

## 🐛 개발 중 발견한 버그와 해결

### 1. 게시글 수정 시 태그 반환값 오류

**문제점**: 태그를 수정하지 않았는데 응답에서 태그가 사라짐

**해결**:
```java
return new PostResponse(post, 
    currentTagSet.equals(newTagSet) ? currentTags : newTags);
```

**배운 점**: null = "수정 안 함" vs [] = "전부 삭제" 명확히 구분

---

### 2. 태그 순서 변경 시 불필요한 재생성

**해결**: Set 비교로 순서 무시
```java
Set<String> currentTagSet = new HashSet<>(currentTags);
Set<String> newTagSet = new HashSet<>(newTags);
```

---

## 🚀 실행 방법

### 1. MySQL 데이터베이스 생성
```sql
CREATE DATABASE writehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. application.yml 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/writehub
    username: root
    password: your_password
```

### 3. 실행
```bash
./gradlew bootRun
```

---

## 📝 완료 현황

### 완료 (100%)
- [x] 프로젝트 설계 (ERD, API 명세)
- [x] 엔티티/Repository 작성
- [x] Member 도메인 (DTO, Service, Controller)
- [x] Post 도메인 (DTO, Service, Controller)
- [x] Follow 도메인 (DTO, Service, Controller)
- [x] Subscription 도메인 (DTO, Service, Controller)

### 개선 예정
- [ ] 전역 예외 처리 (GlobalExceptionHandler)
- [ ] 인증 체크 공통화 (ArgumentResolver)
- [ ] 세션 키 상수화
- [ ] 테스트 코드 작성

---

## 🔜 향후 개선 계획

### 단기 (v1.1)
- 커스텀 예외 + GlobalExceptionHandler
- 인증 체크 중복 제거 (ArgumentResolver)
- 세션 키 상수화

### 중기 (v1.5)
- N+1 문제 최적화
- 조회수 시스템 개선 (Redis)
- Visibility enum 리팩토링 (FOLLOWER_ONLY → SUBSCRIBER_ONLY)

### 장기 (v2.0)
- Spring Security + JWT
- 성능 최적화 (Redis 캐싱, Querydsl)
- 추가 기능 (좋아요, 댓글, 유료 구독, 결제)

---

## 📚 참고 자료

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [김영한님 - 스프링 강의](https://www.inflearn.com/users/@yh)

---

## 👤 개발자

**임동균**  
이메일: sfeagle130@naver.com
노션 포트폴리오: --