# WriteHub

창작자를 위한 구독 기반 기술 블로그 플랫폼

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 개발 기간 | 2026.02.17 ~ 2026.03 |
| 개발 인원 | 1인 (백엔드) |
| 배포 서버 (백엔드) | http://43.203.179.195:8080 |
| 배포 서버 (프론트) | https://writehub-front.vercel.app |
| 테스트 계정 | admin@naver.com / admin1234 |

**기획 의도**

구독 기반 콘텐츠 플랫폼의 핵심 도메인을 직접 설계하고 구현하는 것을 목표로 했습니다. Spring Boot + JPA 기반 실전 프로젝트를 통해 연관관계 매핑, 페이징 처리, N+1 문제 등 실무에서 마주치는 기술적 문제를 직접 해결하는 경험을 쌓았습니다.

---

## 기술 스택

**Backend**
- Java 21, Spring Boot 3.5.10, Spring Data JPA, QueryDSL 5.0, MySQL 8.0

**Frontend**
- React 19, Vite 7, Vercel

**Infrastructure**
- AWS EC2 (Amazon Linux 2023, t3.micro), AWS RDS (MySQL 8.4, db.t4g.micro)
- Docker / DockerHub, GitHub Actions

**Authentication**
- BCrypt 비밀번호 암호화, 세션 기반 인증

---

## 핵심 기능

- 회원 가입 / 로그인 (세션 기반)
- 게시글 CRUD, 공개 범위 설정 (전체 / 구독자 전용)
- 팔로우 / 언팔로우
- 구독 / 구독 취소
- 태그 기반 게시글 관리
- 키워드 / 태그 게시글 검색 (QueryDSL 동적 쿼리)
- 조회수 기능 (본인 제외)
- 닉네임 / 프로필 수정

---

## API 명세

### 회원 (Member) - 7개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/members | 회원가입 | - |
| POST | /api/login | 로그인 | - |
| POST | /api/logout | 로그아웃 | O |
| GET | /api/members | 전체 회원 목록 | - |
| GET | /api/members/me | 내 정보 조회 | O |
| GET | /api/members/{memberId} | 특정 회원 프로필 | - |
| PATCH | /api/members/me | 프로필 수정 | O |

### 게시글 (Post) - 7개

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/posts | 게시글 작성 | O |
| GET | /api/posts | 게시글 목록 | - |
| GET | /api/posts/{postId} | 게시글 상세 | - |
| PUT | /api/posts/{postId} | 게시글 수정 | O |
| DELETE | /api/posts/{postId} | 게시글 삭제 | O |
| GET | /api/members/{memberId}/posts | 특정 회원 게시글 | - |
| GET | /api/posts/search | 게시글 검색 (키워드/태그) | - |

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

## ERD

```
Member (1) ──< (N) Post
Member (1) ──< (N) Follow ──> (1) Member (자기참조)
Member (1) ──< (N) Subscription ──> (1) Member (자기참조)
Post (N) ──< (N) Tag (PostTag 중간 테이블)
Post (1) ──< (N) PostTag (양방향)
```

**주요 설계 결정**
- 기본적으로 단방향 매핑을 유지하되, N+1 해결을 위해 Post → PostTag 방향에 한해 양방향 매핑 추가
- CASCADE 대신 Service Layer에서 명시적 삭제 순서를 관리해 외래 키 제약 조건 에러 방지
- BaseTimeEntity로 생성일 / 수정일 자동 관리

---

## 패키지 구조

```
com.writehub
├── domain
│   ├── member (entity / repository / service / controller / dto)
│   ├── post   (entity / repository / service / controller / dto)
│   ├── follow (entity / repository / service / controller / dto)
│   ├── subscription (entity / repository / service / controller / dto)
│   └── tag    (entity / repository)
└── global
    ├── common
    ├── config
    ├── exception
    └── util
```

---

## 기술적 의사결정

### 1. 게시글 목록 N+1 문제 해결

**문제 상황**

초기 설계는 단방향 매핑으로 `postTagRepository.findByPostId()`를 각 게시글마다 호출하는 구조였습니다. 게시글 4개 조회 시 총 10개의 쿼리가 발생했습니다.

- 게시글 목록 조회 1개
- 작성자 조회 1개
- post_tag 조회 4개 (게시글당 1개)
- tag IN절 조회 4개 (게시글당 1개)

**1차 시도 - batch_fetch_size 적용**

`default_batch_fetch_size: 100` 설정을 적용했으나 효과 없음을 SQL 로그로 확인했습니다. `findByPostId()`는 Hibernate lazy loading이 아닌 명시적 Spring Data JPA 쿼리라 Hibernate가 개입할 수 없는 구조였습니다.

**2차 시도 - QueryDSL fetch join + 2단계 페이징**

페이지네이션 + fetch join 동시 적용 시 Hibernate가 전체 데이터를 메모리에 올리는 문제가 발생해 2단계로 분리했습니다.

- Step 1: ID만 페이징 조회 (offset / limit 적용)
- Step 2: 해당 ID로 author, postTags, tag fetch join
- `PageableExecutionUtils.getPage()`로 마지막 페이지 count 쿼리 생략

**결과**: `getPosts`, `getPostsByAuthor`, `searchPosts` 3개 API 모두 쿼리 10개 → 2개로 감소

**k6 부하테스트 성능 검증**

동일 환경(로컬, 50 VUs, 10분)에서 전후 비교 측정했습니다.

| 항목 | N+1 발생 (최적화 전) | fetch join 적용 (최적화 후) |
|------|------|------|
| 평균 응답시간 | 52.14ms | 16.88ms |
| p(95) | 78.5ms | 32.63ms |
| TPS | 768/s | 2,370/s |
| 총 처리 요청 | 38,442 | 118,514 |
| 에러율 | 0% | 0% |

동일 환경 기준 평균 응답시간 **67% 감소**, TPS **3배 향상**

**배포 서버(EC2 t3.micro) 부하 한계 테스트**

| 항목 | 50 VUs | 100 VUs |
|------|------|------|
| 평균 응답시간 | 76.1ms | 166.06ms |
| p(95) | 160.57ms | 370.59ms |
| TPS | 525/s | 481/s |
| 최대 응답시간 | 729ms | 5.92s |
| 에러율 | 0% | 0% |

100 VUs에서 TPS가 오히려 감소하고 최대 응답시간이 5.92초까지 치솟는 것을 확인했습니다. EC2 t3.micro 환경에서 50 VUs 근처가 포화점임을 직접 검증했습니다. 향후 스케일 아웃 또는 캐싱 도입으로 개선 가능한 지점입니다.

---

### 2. 게시글 태그 수정 전략

태그 수정 시 전체 삭제 후 재생성 + Set 비교 방식을 선택했습니다.

태그 수가 3~5개로 적어 성능 차이가 미미하고, 부분 업데이트 방식은 추가/삭제/유지 3가지 분기를 처리해야 해 코드 복잡도가 높아집니다. Set 비교를 사용하면 순서와 관계없이 태그 변경 여부만 정확하게 판단할 수 있습니다.

JPA 쓰기 지연(write-behind) 특성으로 DELETE/INSERT 순서가 보장되지 않아 `Duplicate entry` 에러가 발생했고, `flush()`로 DELETE를 즉시 실행해 순서를 보장했습니다.

```java
if (!currentTagSet.equals(newTagSet)) {
    postTagRepository.deleteByPostId(postId);
    postTagRepository.flush();  // 즉시 DELETE 실행
    saveTags(post, newTags);
}
```

---

### 3. 인증 방식 - BCrypt + 세션

MVP 단계에서는 구독/팔로우/공개 권한 같은 핵심 도메인 로직 구현에 집중하기 위해 세션 기반 인증을 선택했습니다. Spring Security + JWT는 Refresh Token 관리, Redis 연동 등 추가 설계가 필요해 우선 순위를 조정했습니다.

세션 방식의 한계(Stateful, 수평 확장 어려움)를 인지하고 있으며, 향후 Spring Security + JWT + Refresh Token 방식으로 전환할 예정입니다.

---

### 4. 구독 기반 접근 제어

`Visibility` enum으로 공개 범위를 `PUBLIC` / `SUBSCRIBER_ONLY` 두 타입으로 관리했습니다.

`SUBSCRIBER_ONLY` 게시글 조회 시 ①로그인 여부 → ②본인 여부 → ③구독 여부 순으로 체크하는 3단계 접근 제어를 Service Layer에서 명시적으로 구현했습니다. 비로그인(401) / 미구독(403)을 HTTP 상태코드에 맞게 분리해서 응답합니다.

팔로우(관심 표시)와 구독(콘텐츠 접근권)을 별도 개념으로 분리해 창작자가 콘텐츠 전략을 유연하게 가져갈 수 있도록 설계했습니다. 현재는 무료 구독만 구현되어 있으며, 향후 PG 결제 연동으로 유료 구독 모델로 확장 가능한 구조입니다.

---

### 5. 배포 자동화 - Docker + GitHub Actions CI/CD

초기에는 EC2에서 직접 Gradle 빌드 후 nohup으로 실행하는 방식을 사용했습니다. 두 가지 문제가 있었습니다.

첫째, t3.micro 환경(1GB RAM)에서 빌드 시간이 13분 이상 소요되며 GitHub Actions SSH 연결이 끊기는 타임아웃이 발생했습니다.

둘째, M4 MacBook(ARM)에서 빌드한 이미지가 EC2(x86_64)에서 `no matching manifest for linux/amd64` 에러로 실행되지 않는 아키텍처 불일치 문제가 있었습니다.

**해결**: GitHub Actions에서 빌드를 처리하고 EC2는 실행만 담당하는 역할 분리 구조로 변경했습니다. `--platform linux/amd64` 옵션으로 ARM에서 빌드하더라도 EC2에서 실행 가능한 이미지를 생성했습니다.

**결과**: 배포 시간 13분 이상 → 1~2분으로 단축, git push만으로 빌드 → DockerHub push → EC2 pull → 배포까지 자동화

---

### 6. OOP 원칙 적용 - 엔티티 캡슐화 + 커스텀 예외 계층

`@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 직접 생성을 차단하고, 정적 팩토리 메서드(`createPost()`, `createMember()`)를 통해서만 객체를 생성하도록 강제했습니다.

모든 필드를 private으로 두고 `increaseViewCount()`, `updateProfile()` 같은 비즈니스 의미가 담긴 메서드를 통해서만 값을 변경합니다. `@Setter`는 사용하지 않습니다.

`CustomException`을 abstract 클래스로 만들어 `NotFoundException(404)`, `BadRequestException(400)`, `UnauthorizedException(401)`, `ForbiddenException(403)`, `DuplicateException(409)`로 상황별 예외를 분리했습니다. `GlobalExceptionHandler`에서 `@ExceptionHandler(CustomException.class)` 하나로 모든 자식 예외를 처리합니다.

---

### 7. v1.1 리팩토링 - ArgumentResolver + 세션 상수화

v1.0 완료 후 세션에서 memberId를 꺼내는 코드가 모든 Controller에 반복되는 문제를 발견했습니다. `HandlerMethodArgumentResolver`를 구현한 `LoginMemberArgumentResolver`와 `@LoginMember` 어노테이션으로 memberId를 파라미터로 자동 주입받는 구조로 개선했습니다.

매직 스트링 `"memberId"`를 `SessionConst.MEMBER_ID` 상수로 교체해 오타로 인한 런타임 에러를 컴파일 시점에 방지했습니다.

---

## 트러블슈팅

### 1. SSH 접속 무한 대기 (Hang) 현상

최신 맥북 OpenSSH 10.x 버전이 SSH 패킷에 IPQoS 태그를 붙여 전송하는데, 일부 통신사 네트워크 장비에서 해당 태그를 비정상 트래픽으로 판단해 차단하는 문제였습니다. 서버 문제가 아닌 네트워크 레벨의 패킷 드랍이었습니다.

```bash
ssh -i ~/.ssh/writehub-server.pem -o IPQoS=none ec2-user@[EC2_IP]
```

---

### 2. Gradle Toolchain JDK 인식 불가

EC2에서 `./gradlew bootJar` 실행 시 `Toolchain installation does not provide the required capabilities: [JAVA_COMPILER]` 에러가 발생했습니다. JRE(실행)는 설치되어 있었으나 JDK(컴파일) devel 패키지가 누락된 문제였습니다.

```bash
sudo dnf install java-21-amazon-corretto-devel -y
```

---

### 3. Unknown database 'writehub' 에러

RDS는 MySQL 서버(인스턴스)를 생성하지만 실제 사용할 데이터베이스(schema)는 직접 생성해야 합니다.

```sql
CREATE DATABASE writehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### 4. Spring Boot 틸드(~) 경로 인식 불가

`~`는 bash 쉘에서만 `/home/ec2-user`로 변환됩니다. Spring Boot는 쉘이 아니기 때문에 `~`를 문자 그대로 인식합니다. 절대 경로로 변경해 해결했습니다.

```bash
--spring.config.location=file:/home/ec2-user/application.yml
```

---

### 5. GitHub Actions 빌드 타임아웃

t3.micro(1GB RAM)에서 Gradle 빌드 시 메모리 부족으로 13분 이상 소요되며 SSH 연결이 끊기는 문제였습니다. GitHub Actions에서 빌드 후 Docker 이미지를 DockerHub에 push하고 EC2는 pull하여 실행만 담당하는 구조로 변경했습니다.

---

### 6. Mac M4 ARM 아키텍처 불일치

M4 MacBook(ARM)에서 빌드한 이미지가 EC2(x86_64)에서 실행되지 않는 문제였습니다.

```bash
docker buildx build --platform linux/amd64 -t gyunini/writehub --push .
```

---

### 7. GitHub Actions Secret 오기입

Docker 컨테이너 실행 후 즉시 종료(Exited 1)되며 `Access denied for user 'gyunini'` 에러가 발생했습니다. `DB_USERNAME` Secret에 RDS 계정명(`admin`) 대신 DockerHub 계정명(`gyunini`)을 잘못 입력한 것이 원인이었습니다. `docker logs [컨테이너명]`으로 원인을 확인했습니다.

---

### 8. deploy.yml 한글 오타

`latest` 뒤에 한글 입력 모드로 인한 오타가 삽입되어 `invalid reference format` 에러가 발생했습니다. yml 파일 작성 시 한/영 전환에 주의해야 하며, Secret 값이 마스킹(`***`)되어 표시되므로 오타 확인이 어렵습니다.

---

### 9. Docker 컨테이너 application.yml 미적용

볼륨 마운트 옵션 없이 컨테이너를 실행해 설정 파일이 반영되지 않는 문제였습니다. `-v` 옵션으로 EC2의 application.yml을 컨테이너에 마운트해 해결했습니다.

```yaml
docker run -d \
  -p 8080:8080 \
  -v /home/ec2-user/application.yml:/app/application.yml \
  -e SPRING_CONFIG_LOCATION=file:/app/application.yml \
  --name writehub \
  gyunini/writehub:latest
```

---

### 10. CORS 설정 PATCH 메서드 누락

프로필 수정 요청(PATCH)에서 403 Forbidden이 발생했습니다. 백엔드 로그에 요청이 찍히지 않아 Spring 진입 전에 막힌 것을 파악했고, CORS 설정에 PATCH가 누락된 것이 원인이었습니다.

```java
.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
```

---

### 11. @Transactional(readOnly = true)로 인한 프로필 수정 미반영

프로필 수정 후 새로고침하면 변경사항이 사라지는 문제였습니다. 클래스 레벨 `@Transactional(readOnly = true)` 적용으로 쓰기 메서드가 읽기 전용 트랜잭션으로 동작해 JPA 변경감지가 작동하지 않았습니다. 메서드 레벨에 `@Transactional`을 추가해 해결했습니다.

---

## 실행 방법

### 로컬 실행

```bash
# 1. MySQL 데이터베이스 생성
CREATE DATABASE writehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. application.yml 설정
cp src/main/resources/application-example.yml src/main/resources/application.yml

# 3. 실행
./gradlew bootRun
```

### Docker 실행

```bash
cp docker-compose-example.yml docker-compose.yml
docker-compose up
```

> M1/M2/M3/M4 MacBook 사용자는 EC2 배포 시 반드시 플랫폼을 지정해야 합니다.
> `docker buildx build --platform linux/amd64 -t [계정명]/writehub --push .`

---

## 향후 개선 계획

- Spring Security + JWT 인증 전환 (Stateless, Refresh Token, Redis 토큰 관리)
- Redis 캐싱 도입 (조회수 배치 처리, 프로필 통계)
- Nginx 리버스 프록시 + Blue/Green 무중단 배포
- 좋아요, 댓글 기능
- PG 결제 연동 (유료 구독) + Spring Batch 정산 시스템

---

## 개발자

**임동균**
- 경일대학교 컴퓨터사이언스학부 (GPA 4.37/4.5)
- Email: sfeagle130@naver.com
- GitHub: https://github.com/Dong-gyun-lim