# 우리 아이 금융일기 | Code Convention

## 1. 공통 코드 원칙

- **읽기 쉬운 코드가 우선**이다. 과도한 추상화, 불필요한 공통화는 만들지 않는다.
- 파일과 함수는 한 가지 책임을 갖도록 작성한다. 같은 로직이 반복될 때만 공통화한다.
- 이름만 보고 역할을 알 수 있게 짓는다. `data`, `temp`, `a`, `result2` 같은 이름은 피한다.
- 들여쓰기·따옴표·줄바꿈은 개인 취향보다 자동 포맷터(Prettier, IDE formatter)를 우선한다. 파일을 수정했다면 저장 전 포맷을 실행한다.
- 주석은 코드가 **왜 필요한지** 설명할 때만 한 줄로 작성한다. 코드 내용을 그대로 읽어주는 주석은 쓰지 않는다.
- 기능/정책상 예외 처리는 조용히 무시하지 말고, 사용자 메시지 또는 서버 로그로 확인 가능하게 남긴다.
- API 필드명, 상태값, 공통 용어는 API 명세를 기준으로 통일한다. 명세가 바뀌면 FE·BE 담당자가 함께 반영한다.

### 네이밍 공통

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스·Vue 컴포넌트 | PascalCase | `ChildProfile`, `RoadmapCard.vue` |
| 변수·함수·메서드 | camelCase | `selectedChild`, `fetchRoadmap()` |
| Boolean | `is`, `has`, `can` 등으로 시작 | `isLoading`, `hasInvitation`, `canEdit` |
| 상수 | UPPER_SNAKE_CASE | `MAX_CHILD_COUNT` |
| URL·DB 컬럼·JSON 키 | 소문자 snake_case 또는 API 명세 기준 | `birth_date`, `target_amount` |

---

## 2. 프런트엔드 규칙 (Vue 3 + TypeScript)

### 구조와 파일명

```text
src/
├── api/          기능별 API 호출 모듈
├── components/   재사용 UI 컴포넌트
├── views/        라우트 단위 화면
├── stores/       Pinia 전역 상태
├── composables/  재사용 로직
├── router/       라우팅 설정
├── types/        공용 TypeScript 타입
└── utils/        순수 유틸 함수
```

- 컴포넌트와 화면은 PascalCase: `RoadmapCard.vue`, `ChildRegisterView.vue`
- API 모듈은 camelCase: `childApi.ts`, `roadmapApi.ts`
- Composable은 `use`로 시작: `useChildForm.ts`, `useInvitation.ts`
- Pinia store는 `use도메인Store` 형식: `useAuthStore`, `useChildStore`

### 작성 기준

- Vue 파일은 기본적으로 `<script setup lang="ts">`와 Composition API를 사용한다.
- `views`는 화면 조합과 흐름을, `components`는 재사용 UI를 담당한다.
- Pinia에는 로그인 사용자, 선택된 자녀처럼 여러 화면에서 공유되는 상태만 둔다. 한 화면에서만 쓰는 입력값은 해당 컴포넌트에 둔다.
- API 호출은 `api/`에 모으고, 컴포넌트 안에서 axios 설정을 반복하지 않는다.
- `props`는 부모→자식 데이터 전달, `emit`은 자식→부모 이벤트 전달에 사용한다. 여러 단계 전달이 반복될 때만 store/composable 전환을 검토한다.
- 사용자에게 보이는 로딩·실패·빈 상태를 구현한다. 서버 오류 원문이나 토큰은 화면에 노출하지 않는다.

---

## 3. 백엔드 규칙 (Spring Legacy + MyBatis)

> 이 프로젝트는 **Spring Boot가 아닌 Spring Legacy 기반**으로 개발한다. 아래 규칙은 Spring MVC, MyBatis, XML 설정 기반 구조를 전제로 한다.

### 패키지 구조

도메인 기준으로 묶고, 각 도메인 안에서 역할을 나눈다.

```text
com.ourchildfinance
├── global/                 공통 설정, 예외, 보안, 응답 처리
└── domain/
    ├── child/
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   ├── dto/
    │   └── entity/         필요 시
    └── roadmap/
```

- Controller: 요청 검증, 서비스 호출, HTTP 응답만 담당한다.
- Service: 비즈니스 규칙·권한 확인·트랜잭션을 담당한다.
- Mapper: SQL 실행과 DB 매핑만 담당한다. 비즈니스 판단을 넣지 않는다.
- DTO: 요청과 응답을 분리한다. DB 객체를 API 응답으로 직접 노출하지 않는다.

### 네이밍과 구현 기준

- 클래스는 PascalCase: `ChildController`, `RoadmapService`
- 메서드·변수는 camelCase: `createChild`, `findRoadmapByChildId`
- Request/Response DTO는 목적이 드러나게 작성: `ChildCreateRequest`, `RoadmapResponse`
- Controller URL은 복수형 명사 중심으로 작성하고, API 명세의 URL·HTTP Method를 우선한다.
- 입력값 검증은 Controller DTO에서 하고, 인증·인가와 핵심 소유권 검증은 Service에서 한 번 더 확인한다.
- 예상 가능한 오류는 공통 예외 처리 방식으로 응답한다. 예외를 빈 값으로 숨기지 않는다.
- MyBatis SQL id는 Mapper 메서드명과 맞춘다. SQL은 필요한 컬럼만 조회하고 `SELECT *`는 사용하지 않는다.
- 트랜잭션은 Spring Legacy 프로젝트의 설정 방식(XML 또는 어노테이션)에 맞춰 적용하며, 데이터 변경 또는 여러 DB 작업을 하나로 처리해야 하는 Service 메서드에만 사용한다.
- DispatcherServlet, Root Context, MyBatis, Security 등 공통 XML/Java Config 변경은 영향 범위가 크므로 수정 전 BE 파트장과 공유한다.

---

## 4. 공용 파일·API 변경 규칙

아래 영역은 여러 기능에 영향을 주므로 수정 전 담당 파트장과 공유한다.

- FE: `router/`, 인증 store, axios 인스턴스/인터셉터, `App.vue`, 공통 Layout·공통 컴포넌트
- BE: Security/JWT 설정, 전역 예외 처리, 공통 응답 형식, DB 마이그레이션·공통 Mapper 설정
- 공통: API 명세, 환경 변수 키, 의존성 버전, 배포 설정

- API URL·Request·Response·상태값을 변경하면 **코드 변경보다 API 명세 수정/공유를 먼저** 한다.
- DB 스키마 변경은 변경 SQL 또는 마이그레이션 파일을 PR에 포함하고, 영향받는 API·Mapper를 함께 적는다.
- `.env`, 키 파일, 토큰, 실제 개인정보는 절대 커밋하지 않는다. 필요한 키 목록은 `.env.example`로만 공유한다.
