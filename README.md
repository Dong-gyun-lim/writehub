# WriteHub

창작자를 위한 구독 기반 기술 블로그 플랫폼

## 📋 프로젝트 개요

**개발 기간**: 2025.02.17 ~ 2025.02.21 (약 5일)  
**개발 인원**: 1인 (백엔드)

**기획 의도**:
- 구독 기반 콘텐츠 플랫폼의 핵심 도메인 모델링
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
- **CASCADE**: Service 레이어에서 명시적 삭제 처리
- **BaseTimeEntity**: 생성일/수정일 자동 관리 (@EnableJpaAuditing)

---

## 📁 패키지 구조
```
com.writehub
├── domain
│   ├── member
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   ├── post
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   ├── follow
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   ├── subscription
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   └── tag
│       ├── entity/
│       └── repository/
└── global
    ├── common/
    ├── config/
    ├── exception/
    └── util/
```

---

## 🔗 API 명세

### 회원 (Member) - 7개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members | 회원가입 | - |
| POST | /api/login | 로그인 | - |
| POST | /api/logout | 로그아웃 | O |
| GET | /api/members | 전체 회원 목록 | - |
| GET | /api/members/me | 내 정보 조회 | O |
| GET | /api/members/{memberId} | 특정 회원 프로필 | - |

### 게시글 (Post) - 7개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts | 게시글 작성 | O |
| GET | /api/posts | 게시글 목록 | - |
| GET | /api/posts/{postId} | 게시글 상세 | - |
| PUT | /api/posts/{postId} | 게시글 수정 | O |
| DELETE | /api/posts/{postId} | 게시글 삭제 | O |
| GET | /api/members/{memberId}/posts | 특정 회원 게시글 | - |

### 팔로우 (Follow) - 4개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members/{followingId}/follow | 팔로우 | O |
| DELETE | /api/members/{followingId}/follow | 언팔로우 | O |
| GET | /api/members/{memberId}/following | 팔로잉 목록 | - |
| GET | /api/members/{memberId}/followers | 팔로워 목록 | - |

### 구독 (Subscription) - 4개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members/{creatorId}/subscribe | 구독 | O |
| DELETE | /api/members/{creatorId}/subscribe | 구독 취소 | O |
| GET | /api/members/{memberId}/subscriptions | 구독 목록 | - |
| GET | /api/members/{creatorId}/subscribers | 구독자 목록 | - |

**총 22개 API**

---

## 🧪 API 테스트

### Postman Collection
- [Postman Collection 다운로드](postman/WriteHubAPI.postman_collection.json)
- Import 후 바로 테스트 가능

### 주요 테스트 캡쳐
- [회원가입 성공](docs/member/회원가입.png)
- [로그인 성공](docs/member/로그인.png)
- [게시글 작성](docs/post/게시글_작성.png)
- [구독자 전용 게시글 권한 체크](docs/post/구독자_게시글_조회.png)
- [팔로우 성공](docs/follow/팔로우.png)
- [구독 성공](docs/subscription/구독.png)

---

## 💡 기술적 의사결정

### 1. 게시글 태그 수정 전략

**상황**: 게시글 수정 시 태그를 어떻게 업데이트할 것인가?

**선택**: 전체 삭제 후 재생성 + 변경 감지

**이유**:
- 태그는 보통 3~5개로 적음 (성능 차이 미미)
- 코드 가독성과 유지보수성 우선
- 변경 감지로 불필요한 재생성 방지

**구현**:
```java
Set<String> currentTagSet = new HashSet<>(currentTags);
Set<String> newTagSet = new HashSet<>(newTags);

if (!currentTagSet.equals(newTagSet)) {
    postTagRepository.deleteByPostId(postId);
    postTagRepository.flush();  // 즉시 DELETE 실행
    saveTags(post, newTags);
}
```

**향후 개선**: 태그 개수가 많아지면 Batch Update 고려

---

### 2. 인증 방식: BCrypt + 세션

**선택**: BCrypt + 세션 기반 인증

**이유**:
- 빠른 MVP 구현 (Security 학습 시간 절약, 핵심 기능 집중)
- BCrypt는 업계 표준 암호화 방식
- 설정이 간단하고 안정적

**트레이드오프**:
- 장점: 빠른 개발, 안정적, 학습곡선 낮음
- 단점: Stateful, 확장성 제한

**향후 개선**: Spring Security + JWT + Refresh Token

---

### 3. ResponseEntity 사용

**선택**: ResponseEntity로 HTTP 상태 코드 제어

**이유**:
- RESTful API 설계 원칙 준수
    - 생성(POST): 201 Created
    - 조회(GET): 200 OK
    - 삭제(DELETE): 204 No Content
- 응답 의도가 코드에서 명확히 드러남
- 향후 헤더 추가, 에러 응답 커스터마이징 용이

**트레이드오프**:
- 장점: 명확한 의도 표현, RESTful 원칙 준수
- 단점: 코드 길이 약간 증가 (trade-off 가치 있음)

---

### 4. 게시글 목록 조회 N+1 문제

**상황**: 게시글 목록 조회 시 각 게시글마다 태그 조회 쿼리 발생

**선택**: 현재 상태 유지 (N+1 허용)

**이유**:
- 한 페이지당 10~20개만 조회 (N이 작음)
- 태그 개수도 적음 (게시글당 3~5개)
- 조기 최적화(premature optimization) 방지
- 실제 병목 확인 전까지 단순한 코드 유지

**트레이드오프**:
- 장점: 코드 단순, 유지보수 쉬움
- 단점: 쿼리 수 증가 (페이지당 N+1개)

**향후 개선**:
- 트래픽 증가 시 일괄 조회로 최적화 (쿼리 2개로 감소)
- 또는 태그 정보 캐싱 (Redis)

---

### 5. 프로필 조회 Count 쿼리

**상황**: 회원 프로필 조회 시 5개 쿼리 발생 (follower, following, post, subscriber count)

**선택**: 실시간 COUNT 쿼리

**이유**:
- 프로필 조회 빈도가 높지 않음
- 항상 최신 데이터 반영
- 코드 복잡도 최소화
- 실제 성능 문제 발생 시 개선

**향후 개선**:
- 프로필 조회가 많아지면 Redis 캐싱 도입
- TTL 5분 정도로 설정해서 준실시간 통계

---

### 6. 조회수 증가 방식

**선택**: 조회 즉시 증가 (본인 제외)

**이유**:
- 구현 간단
- 실시간 반영
- 블로그 조회수는 대략적인 수치면 충분
- 트래픽이 초기엔 동시 요청 많지 않음

**트레이드오프**:
- 장점: 빠른 구현, 실시간 반영
- 단점: 새로고침 시 증가, 정확도 낮음

**향후 개선**:
- Redis + 스케줄러로 배치 처리
- 같은 사용자 중복 체크 (24시간 단위)

---

### 7. 공개 범위: 구독 기반

**선택**: 구독자 + 작성자 본인만 조회 가능

**이유**:
- 유료 블로그 플랫폼 특성 (포스타입과 유사한 비즈니스 모델)
- 팔로우는 가벼운 관심 표시 (알림/피드용)
- 구독은 실제 콘텐츠 접근 권한 (수익 모델과 연결)

**구현**:
```java
if (post.getVisibility() == Visibility.SUBSCRIBER_ONLY) {
    if (viewerId == null || 
        (!post.getAuthor().getId().equals(viewerId) &&
         !subscriptionRepository.exists(...))) {
        throw new ForbiddenException("구독자만 볼 수 있는 게시글입니다");
    }
}
```

**향후 개선**: Visibility enum 이름 변경 (FOLLOWER_ONLY → SUBSCRIBER_ONLY)

---

## 🐛 개발 중 발견한 버그와 해결

### 1. 게시글 수정 시 태그 반환값 오류

**문제 상황**:
```java
// 요청에 tags 필드가 없을 때
PUT /api/posts/1
{
  "title": "제목만 수정",
  "content": "내용"
  // tags 없음
}
```

**문제점**: 태그를 수정하지 않았는데 응답에서 태그가 사라짐

**원인**:
```java
List<String> newTags = request.getTags() != null ? request.getTags() : List.of();
return new PostResponse(post, newTags);  // 빈 리스트 반환!
```

**해결**:
```java
return new PostResponse(post, 
    currentTagSet.equals(newTagSet) ? currentTags : newTags);
```

**배운 점**: null = "수정 안 함" vs [] = "전부 삭제" 명확히 구분

---

### 2. 태그 순서 변경 시 불필요한 재생성

**문제**:
```java
currentTags = ["Spring", "Java"]
newTags = ["Java", "Spring"]  // 순서만 바뀜

currentTags.equals(newTags)  → false
// 삭제/재생성 실행됨! (불필요)
```

**해결**: Set 비교로 순서 무시
```java
Set<String> currentTagSet = new HashSet<>(currentTags);
Set<String> newTagSet = new HashSet<>(newTags);
```

**배운 점**: 비즈니스 요구사항에 맞는 자료구조 선택 중요

---

### 3. Validation 설정 오류

**문제 상황**: 회원가입 시 계속 400 Bad Request 발생

**원인**:
```java
@Size(min = 50, max = 50)  // 오타!
private String username;
```

**문제점**:
- `min = 50`으로 설정해서 최소 50자를 요구
- 일반적인 이름은 2~10자인데 50자 이상만 허용
- "임동균" (3글자) → Validation 실패

**해결**:
```java
@Size(max = 50)
@NotBlank
private String username;
```

**배운 점**:
- Validation 어노테이션 설정 시 비즈니스 요구사항 확인 필요
- 에러 메시지만 보고 넘기지 말고 로직 재검토
- 테스트 데이터로 실제 시나리오 검증 중요

---

### 4. JPA Auditing 미활성화

**문제**: createdAt, updatedAt이 null로 저장됨

**원인**: `@EnableJpaAuditing` 설정 누락

**해결**:
```java
@EnableJpaAuditing  // 추가!
@SpringBootApplication
public class WritehubApplication {
    // ...
}
```

**배운 점**: BaseTimeEntity 작성만으로는 부족, 명시적 활성화 필요

---

### 5. 조회수 증가 시 updatedAt 변경

**현재 동작**:
- 게시글 조회 시 조회수 증가
- 조회수 증가 시 updatedAt도 함께 변경됨

**원인**:
- JPA 변경 감지로 UPDATE 쿼리 실행
- BaseTimeEntity의 @LastModifiedDate가 자동 갱신

**향후 개선**:
```java
// @Modifying 쿼리로 viewCount만 증가
@Modifying
@Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
void incrementViewCount(@Param("postId") Long postId);
```
- 또는 Redis 배치 처리로 전환

---

### 6. 본인 게시글 조회 시 조회수 증가

**초기 구현**: 모든 조회 시 무조건 조회수 증가

**문제점**:
- 작성자가 본인 글을 여러 번 확인하면 조회수가 계속 올라감
- 실제 방문자 수와 다른 부정확한 통계

**개선**:
```java
// 본인이 아닐 때만 조회수 증가
if (viewerId == null || !post.getAuthor().getId().equals(viewerId)) {
    post.increaseViewCount();
}
```

**효과**:
- 작성자 본인: 조회수 증가 X
- 다른 사람: 조회수 증가 O
- 비로그인 사용자: 조회수 증가 O

**향후 개선**:
- IP 기반 중복 체크 (24시간)
- Redis 캐싱으로 배치 처리

---

### 7. 게시글 수정 시 태그 중복 키 에러

**문제 상황**: 게시글 수정 시 `Duplicate entry '1-1' for key 'post_tag'` 에러 발생

**원인**:
```java
if (!currentTagSet.equals(newTagSet)) {
    postTagRepository.deleteByPostId(postId);  // DELETE 명령
    saveTags(post, newTags);  // INSERT 명령
}
// 트랜잭션 커밋 시 쿼리 실행 순서가 보장 안 됨
// → INSERT가 먼저 실행되면 중복 키 에러!
```

JPA는 트랜잭션 커밋 시점에 쿼리를 실행하는데, INSERT와 DELETE의 순서가 보장되지 않아 INSERT가 먼저 실행되면 기존 데이터와 충돌

**해결**:
```java
if (!currentTagSet.equals(newTagSet)) {
    postTagRepository.deleteByPostId(postId);
    postTagRepository.flush();  // ← 즉시 DELETE 실행!
    saveTags(post, newTags);
}
```

**배운 점**:
- JPA의 쓰기 지연(write-behind) 특성 이해
- flush()를 통한 즉시 실행 제어
- 트랜잭션 내 쿼리 실행 순서의 중요성

---

### 8. 게시글 삭제 시 외래 키 제약 조건 에러

**문제 상황**: 게시글 삭제 시 `Cannot delete or update a parent row: a foreign key constraint fails` 에러 발생

**원인**:
```java
// PostTag가 Post를 참조하고 있음
postRepository.delete(post);  
// → PostTag가 남아있어서 삭제 불가!
```

ERD 설계 시 CASCADE 설정을 계획했으나, 실제 구현에서 누락됨
- JPA에 Cascade 설정 안 함 (단방향 매핑 사용)
- DB에도 ON DELETE CASCADE 설정 안 함

**해결**:
```java
@Transactional
public void deletePost(Long postId, Long authorId) {
    Post post = postRepository.findById(postId).orElseThrow();
    
    // 작성자 확인
    if (!post.getAuthor().getId().equals(authorId)) {
        throw new ForbiddenException("본인의 게시글만 삭제할 수 있습니다");
    }
    
    // 1. PostTag 먼저 삭제 (자식)
    postTagRepository.deleteByPostId(postId);
    
    // 2. Post 삭제 (부모)
    postRepository.delete(post);
}
```

**배운 점**:
- 외래 키 관계에서 삭제 순서의 중요성
- CASCADE 설정의 필요성 인지
- Service 레이어에서 명시적 삭제 순서 관리
- 설계와 구현 사이의 간극 확인 필요

**설계 결정**:
- JPA Cascade 대신 Service에서 명시적 처리 선택
- 이유: 단방향 매핑 유지, 삭제 로직 명확성
- 향후: 회원 삭제 등 복잡한 케이스에서도 동일 패턴 적용 예정

---

### 9. 파라미터 순서 실수

**문제 상황**: 여러 메서드에서 파라미터 순서를 잘못 전달하여 런타임 에러 발생

**사례 1: 게시글 삭제**
```java
// 잘못된 호출
postService.deletePost(authorId, postId);  // 순서 반대!

// 올바른 호출
postService.deletePost(postId, authorId);
```

**사례 2: 구독 취소 응답**
```java
// 잘못된 응답
return new SubscriptionResponse(subscription.getId(), subscriberId, creatorId);
// → 3개 파라미터 = "구독 성공" 생성자 호출!

// 올바른 응답
return new SubscriptionResponse(subscriberId, creatorId);
// → 2개 파라미터 = "구독 취소 성공" 생성자 호출
```

**원인**:
- 비슷한 타입(Long)의 파라미터가 여러 개
- 컴파일 시점에 에러 없음 (타입은 맞음)
- 런타임에서만 논리 오류 발견

**배운 점**:
- 파라미터 순서에 주의
- 메서드 명명 규칙 일관성 유지 (postId 항상 먼저 등)
- 빌더 패턴 또는 DTO 파라미터 고려
- 단위 테스트의 중요성

**향후 개선**:
- 파라미터가 3개 이상이면 DTO로 래핑
- 메서드 네이밍 컨벤션 문서화

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

### 4. Postman으로 API 테스트
1. `postman/WriteHub.postman_collection.json` 파일을 Postman에 Import
2. 회원가입 → 로그인 → 게시글 작성 순서로 테스트

---

## 📝 완료 현황

### 완료 (100%)
- [x] 프로젝트 설계 (ERD, API 명세)
- [x] 엔티티/Repository 작성 (전체)
- [x] Member 도메인 (7개 API)
- [x] Post 도메인 (7개 API)
- [x] Follow 도메인 (4개 API)
- [x] Subscription 도메인 (4개 API)
- [x] Postman Collection 작성
- [x] API 테스트 완료
- [x] v1.1 리팩토링 (예외처리, ArgumentResolver, 세션상수화)

**총 22개 API 완성**

---

## 🔜 향후 개선 계획

### 단기 v1.1 (완료)

**1. 전역 예외 처리 (GlobalExceptionHandler)**
- RuntimeException, ResponseStatusException → 커스텀 예외 계층 구조로 교체
- CustomException 기반 5가지 예외 클래스 (400/401/403/404/409)
- GlobalExceptionHandler로 일관된 에러 응답 처리

**2. 인증 체크 중복 제거 (ArgumentResolver)**
- Controller마다 반복되던 세션 체크 코드 제거
- @LoginMember 어노테이션으로 memberId 자동 주입

**3. 세션 키 상수화**
- 매직 스트링 "memberId" → SessionConst.MEMBER_ID 상수로 교체
- 오타 방지 및 변경 시 단일 수정 포인트 확보

---

### 중기 (v1.5)

**1. N+1 문제 최적화**
- 게시글 목록 조회 시 일괄 조회 (쿼리 2개)
- Querydsl 도입 검토

**2. 조회수 시스템 개선**
- Redis + 스케줄러 배치 처리
- IP/User 기반 중복 체크 (24시간)

**3. Visibility enum 리팩토링**
- `FOLLOWER_ONLY` → `SUBSCRIBER_ONLY`로 명확하게 변경

---

### 장기 (v2.0)

**1. Spring Security + JWT**
- Stateless 인증으로 전환
- Refresh Token 발급
- Redis를 이용한 토큰 관리

**2. 성능 최적화**
- Redis 캐싱 (프로필 통계, 조회수)
- Querydsl 도입
- DB 인덱싱 최적화

**3. 추가 기능**
- 좋아요, 댓글 기능
- PG 결제 연동 (유료 구독)
- Spring Batch를 이용한 정산 시스템
- 알림 기능 (SSE 또는 WebSocket)

---

## 📚 참고 자료

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [김영한님 - 스프링 강의](https://www.inflearn.com/users/@yh)

---

## 👤 개발자

**임동균**
- 경일대학교 컴퓨터공학과 (GPA 4.37/4.5)
- Email: sfeagle130@naver.com
- GitHub: https://github.com/Dong-gyun-lim
