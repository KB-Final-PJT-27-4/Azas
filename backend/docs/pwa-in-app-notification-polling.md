# PWA 인앱 알림 폴링 계약

이 문서는 활성 상태의 웹/PWA 클라이언트가 알림을 준실시간으로 조회하고,
백그라운드에서 복귀했을 때 누락된 알림을 복구하는 백엔드 계약을 설명한다.
PWA가 종료된 상태에서 시스템 알림을 표시하는 Web Push/FCM은 이 범위에
포함하지 않는다.

## 최초 동기화

로그인 직후 다음 API로 최신 알림과 폴링 시작점을 조회한다.

```http
GET /api/v1/notifications?size=50
Authorization: Bearer <access-token>
```

알림이 한 건도 없어도 `poll_cursor`는 `0`이다. 응답의
`recommended_poll_interval_seconds`는 현재 15초이며, 활성 화면에서 다음
폴링을 예약할 때 사용한다. 최초 동기화 항목에는 새 알림 토스트를 표시하지
않는다.

## 신규 알림 폴링

마지막 응답의 `poll_cursor`를 `after_id`로 전달한다.

```http
GET /api/v1/notifications?after_id=42&size=20
Authorization: Bearer <access-token>
```

- `items`에는 `notification_id > after_id`인 현재 회원의 알림만 포함된다.
- `poll_cursor`는 이번 응답에서 확인한 가장 큰 알림 ID다.
- 새 알림이 없으면 전달한 `after_id`가 그대로 반환된다.
- `has_more_new=true`이면 갱신된 `poll_cursor`로 즉시 다시 요청해 누적된
  알림을 모두 비운다.
- `unread_count`는 헤더 배지의 서버 기준 값이다.

응답은 `Cache-Control: no-store`, `Pragma: no-cache`,
`Vary: Authorization`을 포함한다. PWA 서비스 워커도 알림 API를 별도로
캐시하지 않아야 한다.

## PWA 수명 주기

1. 문서가 `visible`이면 권장 간격으로 폴링한다.
2. 문서가 `hidden`이면 타이머를 중단한다.
3. 다시 `visible`이 되면 간격을 기다리지 않고 즉시 `after_id` 요청한다.
4. 요청이 겹치지 않게 클라이언트에서 한 번에 하나만 실행한다.
5. 로그아웃 시 타이머와 회원별 알림 상태를 초기화한다.

이 방식은 시간 대신 단조 증가하는 `notification_id`를 사용하므로 클라이언트
시계 차이와 무관하게 복귀 중 누락분을 회수할 수 있다.

## 용돈 요청 알림

- 자녀가 요청하면 연결된 활성 부모마다 `ALLOWANCE_REQUESTED` 알림을 만든다.
- 부모가 승인하면 자녀에게 `ALLOWANCE_APPROVED` 알림을 만든다.
- 부모가 거절하면 자녀에게 `ALLOWANCE_REJECTED` 알림을 만든다.
- 자녀가 요청을 취소할 때는 별도 상태 알림을 만들지 않는다.
- `ALLOWANCE` 수신 설정이 꺼진 회원은 알림 생성 대상에서 제외한다.
- `deduplication_key`의 유니크 제약으로 동일 업무 이벤트의 중복 저장을 막는다.

## DB 연결 풀

반복 조회가 매번 새 물리 DB 연결을 생성하지 않도록 HikariCP를 사용한다.
기본값은 최대 10개, 최소 유휴 2개이며 다음 환경변수로 조정한다.

```text
DB_MAX_POOL_SIZE
DB_MIN_IDLE
DB_CONNECTION_TIMEOUT_MILLIS
DB_VALIDATION_TIMEOUT_MILLIS
DB_IDLE_TIMEOUT_MILLIS
DB_MAX_LIFETIME_MILLIS
DB_INITIALIZATION_FAIL_TIMEOUT_MILLIS
```
