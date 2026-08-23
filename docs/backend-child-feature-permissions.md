# 자녀 이용 권한 API

부모가 연결된 자녀의 기능 사용 권한을 관리합니다. 사용 금액 한도 자체는 기존
`ACCOUNT-11` 자녀 사용 관리 정책 API에서 관리하며, 이 API는 자녀 화면에서 해당
기능을 노출·사용할 수 있는지 관리합니다.

## 권한 조회

```http
GET /api/v1/children/{child_id}/feature-permissions
Authorization: Bearer {access_token}
```

부모만 호출할 수 있습니다.

### 성공 응답

```json
{
  "child_id": 6,
  "allowance_request_enabled": true,
  "usage_limit_view_enabled": true
}
```

## 권한 수정

```http
PATCH /api/v1/children/{child_id}/feature-permissions
Authorization: Bearer {access_token}
Content-Type: application/json
```

```json
{
  "allowance_request_enabled": true,
  "usage_limit_view_enabled": false
}
```

두 필드는 모두 필수입니다. 수정 성공 시 조회 API와 같은 응답을 반환합니다.

## 동작 영향

- `allowance_request_enabled=false`: 자녀의 용돈 계획 요청 생성은 `403 ALLOWANCE_REQUEST_DISABLED`로 거절됩니다.
- `usage_limit_view_enabled=false`: 자녀의 사용 가능 금액 조회는 `403 CHILD_USAGE_LIMIT_VIEW_DISABLED`로 거절됩니다.
- 부모의 자녀 이용 권한 조회·수정은 접근 가능한 자녀에 대해서만 가능합니다.

## 오류 응답

- `400 BADREQUEST`: PATCH 본문에서 두 권한 값 중 하나가 누락되었거나 형식이 올바르지 않은 경우
- `401 INVALID_ACCESS_TOKEN`: Access Token이 없거나 유효하지 않은 경우
- `403 CHILD_ACCESS_DENIED`: 요청 부모가 해당 자녀에 접근할 권한이 없는 경우
- `404 CHILD_NOT_FOUND`: 자녀가 존재하지 않는 경우

## RDS 반영

RDS에는 기존 개별 파일 대신 다음 통합 파일 하나만 실행합니다.

```text
backend/src/main/resources/db/migration/V20260823__apply_pending_rds_auto_transfer_and_child_permission_changes.sql
```

이 파일은 아직 반영되지 않은 부모 적금 자동이체 허용 변경과 자녀 이용 권한 컬럼을 함께 적용합니다.
