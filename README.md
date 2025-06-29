# 🚀 DDOBAK Server

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-316192?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![AWS](https://img.shields.io/badge/AWS-Lambda%20%7C%20S3-FF9900?style=flat-square&logo=amazon-aws&logoColor=white)](https://aws.amazon.com/)

> **계약서 OCR 및 분석 서비스 백엔드 API 서버**

## 🎯 프로젝트 소개

계약서 이미지를 업로드하면 OCR로 텍스트를 추출하고, AI로 독소조항을 분석하는 Spring Boot REST API 서버입니다.

### 🔧 기술 스택
- **백엔드**: Java 17, Spring Boot 3.5.0, Spring Data JPA
- **데이터베이스**: PostgreSQL (운영), H2 (테스트)
- **클라우드**: AWS Lambda (OCR/분석), S3 (파일 저장)
- **기타**: HikariCP, JWT, OAuth2

## 📁 프로젝트 구조

```
src/main/java/com/sbpb/ddobak/server/
├── common/                    # 공통 모듈
│   ├── exception/            # 예외 처리 (GlobalExceptionHandler)
│   ├── response/             # API 응답 구조 (ApiResponse)
│   └── utils/                # 유틸리티 (S3Util, LambdaUtil, ...)
├── config/                   # 설정 클래스
│   └── AwsConfig.java        # AWS 클라이언트 설정
├── domain/                   # 도메인별 패키지
│   ├── auth/                 # 인증/인가
│   ├── documentProcess/      # 계약서 처리 (OCR, 분석)
│   │   ├── controller/       # REST API 컨트롤러
│   │   ├── dto/             # 요청/응답 DTO
│   │   ├── entity/          # JPA 엔티티
│   │   ├── repository/      # 데이터 접근
│   │   └── service/         # 비즈니스 로직
│   ├── user/                # 사용자 관리
│   └── externalContent/     # 외부 콘텐츠
└── ServerApplication.java    # 메인 클래스
```

## 🚀 시작하기

### 1. 환경 설정

#### 1.1 데이터베이스 설정
프로젝트는 **프로파일 기반**으로 두 가지 데이터베이스 환경을 지원합니다:

**🔸 PostgreSQL (운영 환경)**
```bash
# .env 파일에 PostgreSQL 연결 정보 설정
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ddobak_db
DB_USERNAME=ddobak_user
DB_PASSWORD=your_password
```

**🔸 H2 (테스트 환경)**
- 별도 설정 불필요 (인메모리 데이터베이스)

#### 1.2 AWS 설정
```bash
# .env 파일에 AWS 정보 설정
SERVICE_BUCKET=your-service-bucket
TEST_BUCKET=your-test-bucket
```

### 2. 프로젝트 실행
```bash
# 프로젝트 클론
git clone https://github.com/ddobak/DDOBAK-Backend-Core.git
cd DDOBAK-Backend-Core

# PostgreSQL로 실행 (기본)
./gradlew bootRun

# H2 테스트 DB로 실행
./gradlew bootRun --args='--spring.profiles.active=test'
```

### 3. API 테스트
```bash
# 헬스체크
curl http://localhost:8080/h2-console  # H2 콘솔 (test 프로파일)

## 👥 팀 작업 방식

> **Issue 기반 개발 워크플로우**를 따릅니다. 모든 작업은 Issue에서 시작하여 PR로 완료됩니다.

### 🌳 브랜치 사용법
```bash
main       # 배포용
  ├── dev  # 개발 메인 (앱스토어 배포 전까지 사용 X)
  ├── feat/#이슈번호-기능명  # 새 기능 개발
  └── fix/#이슈번호-버그명   # 버그 수정
  └── ...
```

### 📝 브랜치 이름 예시 (Issue 번호 포함)
```bash
feat/12-user-login     # Issue #12: 로그인 기능
feat/15-file-upload    # Issue #15: 파일 업로드
fix/23-login-bug       # Issue #23: 로그인 버그 수정
```

### 💬 커밋 메시지 (Issue 번호 포함)
```bash
feat: 로그인 기능 추가 #12
fix: 회원가입 오류 수정 #23
docs: README 업데이트 #25
```

## 🔄 개발 흐름 (Issue → Branch → PR)

1. **Issue 생성/확인** → GitHub Issues에서 작업할 이슈 생성 또는 할당받기
2. **브랜치 생성** → `feat/이슈번호-기능명` 으로 Issue 기반 브랜치 만들기
3. **코딩** → Issue 요구사항에 맞는 기능 개발
4. **테스트** → 로컬에서 잘 돌아가는지 확인
5. **PR 생성** → Pull Request 올리기 (Issue 번호 연결)
6. **코드 리뷰** → 팀원들이 코드 확인
7. **머지** → dev 브랜치에 합치기 후 Issue 자동 닫힘 (앱스토어 배포 전까지 main에 바로 반영)

### 📋 PR 작성 시 Issue 연결
```markdown
<!-- PR 템플릿 예시 -->
## 관련 Issue
Closes #12

## 변경사항
- ...
```

## 📚 개발 규칙

### 🎯 Cursor Rules
Cursor 활용성 극대화를 위해 `.cursor/rules/` 폴더에 rule들을 구성했습니다.  
프로젝트를 진행하면서, 필요 시 rule은 계속해서 수정될 수 있습니다.

### 📖 문서 구조
```
docs/
├── api-development-guide.md  # API 개발 가이드 (Response/Exception 처리)
└── (추후 추가 예정...)
```

## 📚 개발 가이드

### 🔧 API 개발 시 참고사항
- **Response/Exception 처리**: [API 개발 가이드](docs/api-development-guide.md) 참고
- **Common 모듈 활용**: `ApiResponse<T>`, 예외 클래스들 필수 사용
- **에러 코드 체계**: 2xxx(성공), 4xxx(클라이언트), 5xxx(서버) 분류 준수


### 🔧 빌드 및 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew testS3      # S3 관련 테스트
./gradlew testLambda  # Lambda 관련 테스트

# 빌드
./gradlew build
```
