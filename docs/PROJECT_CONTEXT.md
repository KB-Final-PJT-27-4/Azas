# 아자스(Azas) 프로젝트 컨텍스트

> 이 문서는 구현 AI와 새 팀원이 프로젝트의 **현재 범위·데이터 모델·API 방향**을 빠르게 이해하기 위한 압축 컨텍스트다. 완전한 ERD나 상세 API 명세를 대체하지 않는다.
>
> 기준일: 2026-07-30. 원본: Notion `확정 ERD V2`, `API 명세서 (1)`, `요구 기능 명세서_개정안 (1)`.

## 1. 서비스와 구현 원칙

아자스는 부모와 자녀가 함께 사용하는 자녀 자산관리 서비스다. 부모가 자녀 프로필·계좌·적금을 관리하고, 적금 납입의 의미를 사진·영상·편지와 함께 타임캡슐로 남긴다. 아이 계정은 부모가 직접 만드는 계정이 아니라, **초대코드로 진입한 뒤 소셜 로그인하여 기존 자녀 프로필에 연결되는 계정**이다.

- API 기본 경로는 `/api/v1`이다.
- 사용자 역할은 `ADULT`, `CHILD`, `ADMIN`이다. 모든 자녀 리소스는 부모-자녀 연결 또는 아이 본인 연결을 확인한 뒤 접근시킨다.
- 초기 가입에서 부모는 자녀 **프로필만** 생성한다. 아이 `member` 계정은 초대 수락 후에 `child.member_id`로 연결된다.
- 민감한 값(계좌번호·전화번호·외부 연결 식별자)은 평문 저장하지 않는다. 암호문과 중복/검색용 해시를 분리한다.
- 명세에 없는 테이블·엔드포인트·외부 연동을 임의로 추가하지 않는다. 특히 실제 은행 이체는 별도 승인 전까지 내부/Mock 이체 흐름으로 취급한다.

## 2. 범위 판단 기준

구현 범위는 요구 기능 명세의 `구분=MVP`와 `기능=필수`를 우선한다. API 명세에는 향후 구현용 엔드포인트도 포함되어 있으므로, API가 존재한다는 이유만으로 구현 범위에 넣지 않는다.

| 우선 구현(MVP) | 후순위 또는 미확정 |
| --- | --- |
| 부모·아이 소셜 로그인, 역할 분기, 가족 초대/연동 | 자동이체 |
| 자녀 프로필과 공동 부모 관리 | 금융상품 추천·북마크 |
| 부모/자녀 계좌 조회·권한 설정·이체·적금 납입 | 체크리스트 |
| 적금 기반 타임캡슐 생성·조회·공개 | 월간 자산 리포트, 게시판/댓글, 알림 |
| 자녀/부모 대시보드의 핵심 조회 | 전화번호 SMS 인증·카카오 본인인증 — `고민` 상태 |

## 3. 핵심 사용자 흐름

1. 부모가 소셜 로그인한다.
2. 부모가 자녀 기본 정보를 등록한다. 이때 아이 회원 계정은 없다.
3. 부모는 공동 부모 또는 아이용 가족 초대코드를 발급한다.
4. 초대받은 사람이 소셜 로그인한다.
   - `PARENT` 초대 수락: `child_parent` 관계를 생성한다.
   - `CHILD` 초대 수락: 기존 `child.member_id`에 아이 회원을 연결한다.
5. 부모가 계좌를 연결·선택하고, 아이 명의 입출금/적금 계좌를 관리한다.
6. 부모가 아이 계좌 또는 적금 계좌로 이체·납입한다.
7. 적금 계좌에 연결된 타임캡슐에 입금 거래와 메시지·미디어를 기록하고 봉인한다.
8. 적금이 만기 또는 해지되면 타임캡슐을 공개한다.

## 4. 데이터 모델 요약

### 계정·가족

| 엔터티 | 역할과 핵심 관계 |
| --- | --- |
| `member` | 로그인 계정. 이메일은 고유하며 역할은 `ADULT`/`CHILD`/`ADMIN`이다. |
| `social_account` | 소셜 공급자와 공급자 사용자 식별자를 `member`에 연결한다. |
| `child` | 부모가 먼저 등록하는 자녀 프로필. 아이가 가입 전이면 `member_id`는 `NULL`이다. |
| `child_parent` | 성인 회원과 자녀의 N:M 관계. `(child_id, member_id)`는 중복될 수 없다. |
| `family_invitation` | 부모/아이 초대를 통합 관리한다. 원문 토큰은 저장하지 않고 해시만 보관한다. |

### 금융·계좌·거래

| 엔터티 | 역할과 핵심 관계 |
| --- | --- |
| `financial_connection` | 사용자 동의 기반 금융 연결. 연결 행위자, 자녀, 소유자 유형, 동의 상태를 관리한다. |
| `financial_account` | 서비스에서 사용하는 부모/자녀 계좌. 입출금·적금·청약 유형, 잔액, 연결 상태를 보관한다. |
| `financial_sync_job` | 외부 금융정보 동기화 요청과 결과를 비동기로 관리한다. |
| `account_balance_snapshot` | 계좌별 잔액 변화 이력. |
| `account_transaction` | 계좌 입출금 거래와 거래 후 잔액. 타임캡슐 기록의 원천이다. |
| `financial_transfer` | 출금 계좌와 입금 계좌 사이의 이체 요청/결과. `idempotency_key`로 중복 이체를 막는다. |
| `auto_transfer_schedule` | 자동이체 일정. 데이터 모델과 API는 있으나 현재 후순위다. |

#### 계좌와 목표·자녀 권한 규칙

- **독립 `financial_goal` 테이블을 새로 만들지 않는다.** 선택 목표는 적금 `financial_account`에 연결한다.
  - 필요 시 `financial_goal_template_id`, `goal_name_snapshot`, `goal_target_amount`, `goal_target_date`를 사용한다.
- 아이 입출금 계좌의 접근 모드는 `CO_MANAGED` 또는 `UNRESTRICTED`다.
  - `CO_MANAGED`: 부모가 `child_available_amount`를 정하고 아이에게는 사용 가능 금액 중심으로 노출한다.
  - `UNRESTRICTED`: 제한 없이 잔액을 보여준다.
- 이체는 출금·입금·잔액·권한을 하나의 트랜잭션으로 검증한다. 아이 요청 이체는 사용 가능 금액을 원자적으로 검증·차감한다.

### 타임캡슐

```text
적금 financial_account 1 ── 1 time_capsule
time_capsule 1 ── N time_capsule_entry
time_capsule_entry 1 ── N time_capsule_media
```

| 엔터티 | 역할과 핵심 규칙 |
| --- | --- |
| `time_capsule` | **적금 계좌 하나를 대표하는 보관함**이다. 상태는 `COLLECTING`/`RELEASED`/`ARCHIVED`다. 메시지·미디어를 직접 저장하지 않는다. |
| `time_capsule_entry` | 적금 계좌의 입금 거래 하나에 연결한 부모/보호자의 메시지·미디어 기록이다. `DRAFT`/`SEALED`/`DELETED` 상태를 가진다. |
| `time_capsule_media` | 엔트리별 첨부 파일의 객체 키와 업로드 상태를 보관한다. 사진은 최대 3장 또는 영상은 1개이며 사진·영상 혼합은 허용하지 않는다. |
| `time_capsule_export` | 공개된 보관함의 영상/아카이브 결과물 생성 비동기 작업. 현재 핵심 MVP 완료 후 다룬다. |

타임캡슐 불변 규칙:

- 부모/보호자만 공개 전 기록 내용·미디어를 열람한다. 아이는 공개 전 제목과 D-Day만 볼 수 있다.
- 엔트리는 타임캡슐에 연결된 **같은 적금 계좌의 입금 거래**만 선택할 수 있다.
- `(time_capsule_id, account_transaction_id)` 조합은 고유하다.
- `SEALED` 엔트리만 공개 및 결과물 생성 대상이다.
- 현재 확정 ERD의 공개 사유는 `MATURITY`(만기)와 `TERMINATION`(해지)다. 요구 기능 명세에 남아 있는 `특별한 날` 공개 선택지는 ERD·API 모델에 없으므로, 스키마/API 합의 전에는 구현하지 않는다.

### 기타 후순위 엔터티

`checklist_item_template`, `child_checklist_item`, `financial_product`, `financial_product_bookmark`, `asset_report`, `notification_preference`, `notification`은 확장을 고려한 모델이다. 현재 MVP 핵심 흐름을 막지 않는 한 신규 기능 구현 우선순위는 낮다.

## 5. API 계약 요약

상세 요청/응답 필드는 Notion API 명세를 따르되, 새 구현은 아래 리소스 구조와 경로를 유지한다. ID path parameter는 현재 명세처럼 `snake_case`를 사용한다.

### 인증·회원

| 목적 | 대표 엔드포인트 |
| --- | --- |
| 부모/일반 소셜 로그인 | `POST /api/v1/auth/oauth/{provider}` |
| 아이 초대 기반 소셜 로그인 | `POST /api/v1/auth/oauth/{provider}/child-invite` |
| 토큰 재발급·로그아웃 | `POST /api/v1/auth/token/refresh`, `POST /api/v1/auth/logout` |
| 내 정보·역할·탈퇴 | `GET/PATCH/DELETE /api/v1/members/me`, `GET /api/v1/members/me/role` |

### 자녀·가족

| 목적 | 대표 엔드포인트 |
| --- | --- |
| 자녀 프로필 | `POST/GET /api/v1/children`, `GET/PATCH/DELETE /api/v1/children/{child_id}` |
| 가족 조회 | `GET /api/v1/children/{child_id}/family-members` |
| 가족/아이 초대 발급 | `POST /api/v1/children/{child_id}/family-invitations` |
| 초대 검증·수락 | `GET /api/v1/family-invitations/{invite_token}`, `POST /api/v1/family-invitations/{invite_token}/accept` |

### 금융 연결·계좌·이체

| 목적 | 대표 엔드포인트 |
| --- | --- |
| 금융 연결·동기화 | `POST/GET /api/v1/members/me/financial-connections`, `POST /api/v1/financial-connections/{connection_id}/sync` |
| 자녀/부모 계좌 조회 | `GET /api/v1/children/{child_id}/accounts`, `GET /api/v1/members/me/accounts` |
| 계좌 상세·대표 설정 | `GET /api/v1/accounts/{account_id}`, `PATCH /api/v1/accounts/{account_id}/primary` |
| 아이 계좌 권한 | `GET/PATCH /api/v1/accounts/{account_id}/child-access-policy`, `GET /api/v1/children/me/available-amount` |
| 적금 계좌 선택 목표 | `PATCH/DELETE /api/v1/accounts/{account_id}/financial-goal` |
| 이체·내역 | `POST /api/v1/transfers`, `GET /api/v1/members/me/transfers`, `GET /api/v1/children/{child_id}/transfers` |

### 타임캡슐

| 목적 | 대표 엔드포인트 |
| --- | --- |
| 적금 기반 보관함 생성·조회 | `POST /api/v1/accounts/{account_id}/time-capsule`, `GET /api/v1/children/{child_id}/time-capsules` |
| 보관함·기록 조회 | `GET /api/v1/time-capsules/{time_capsule_id}`, `GET /api/v1/time-capsules/{time_capsule_id}/entries` |
| 기록 생성·수정·봉인 | `POST /api/v1/time-capsules/{time_capsule_id}/entries`, `PATCH /api/v1/time-capsule-entries/{entry_id}`, `PATCH /api/v1/time-capsule-entries/{entry_id}/seal` |
| 미디어 업로드 처리 | `POST /api/v1/time-capsule-entries/{entry_id}/media/upload-urls`, `POST /api/v1/time-capsule-entries/{entry_id}/media/complete` |

## 6. 구현 시 하지 말 것

- 구버전 일반 회원가입 `POST /auth/register`를 되살리지 않는다. 로그인/가입은 소셜 OAuth 흐름으로 통합한다.
- 부모가 아이의 `member` 계정을 임의 생성하지 않는다.
- 초대 토큰 원문, 계좌번호, 외부 연결 식별자를 평문으로 저장하거나 로그에 남기지 않는다.
- 개인 기능 브랜치의 임시 데이터 모델을 공용 ERD보다 우선하지 않는다.
- `후순위`인 자동이체·체크리스트·리포트·커뮤니티·상품 추천을 MVP 핵심 구현으로 가정하지 않는다.
- 인증 명세의 전화번호/SMS/카카오 본인인증은 아직 결정되지 않았으므로 선행 구현하지 않는다.

## 7. 명세 충돌 시 우선순위

1. 요구 기능 명세서의 **MVP/필수 여부**로 구현 범위를 결정한다.
2. 확정 ERD V2로 테이블·관계·상태값을 결정한다.
3. API 명세서 (1)로 HTTP method와 URL을 결정한다.
4. 세 문서가 충돌하거나 빈 값이면 추정 구현하지 말고 팀에 확인한다.

