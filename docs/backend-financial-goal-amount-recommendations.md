# 목표 금액 추천 API

## 조회

```http
GET /api/v1/financial-goal-templates/{financial_goal_template_id}/amount-recommendations
Authorization: Bearer {access_token}
```

기본 목표 템플릿에 대해 공공 통계를 참고해 서비스가 구성한 4단계 추천 금액을 반환합니다. 공공 통계가 추천 금액을 직접 제공하는 것은 아니므로 화면에는 `공공 통계를 참고한 서비스 추천 금액`으로 표시해야 합니다.

### 성공 응답

```json
{
  "financial_goal_template_id": 1,
  "goal_name": "대학자금",
  "recommendation_method": "STATISTICS_REFERENCE",
  "reference_data": {
    "organization": "교육부·한국대학교육협의회",
    "dataset_name": "2025년 4월 대학정보공시 분석 결과",
    "reference_year": 2025,
    "metric_name": "4년제 일반·교육대학 1인당 연평균 등록금",
    "metric_value": 7106500,
    "metric_unit": "원/년",
    "source_url": "https://www.moe.go.kr/boardCnts/viewRenew.do?boardID=294&boardSeq=103257&lev=0&m=020402&opType=N"
  },
  "description": "연평균 등록금 7,106,500원의 4년치 28,426,000원을 기본 근거로 교육·생활·사회초년 비용을 단계별로 구성했습니다.",
  "disclaimer": "공공 통계를 참고한 서비스 추천금액이며 실제 대학과 생활 방식에 따른 비용 또는 목표 달성을 보장하지 않습니다.",
  "recommendations": [
    {
      "recommendation_code": "STARTER",
      "title": "시작 준비안",
      "target_amount": 30000000,
      "coverage_items": ["4년 등록금 중심", "교재 및 학습비 일부"],
      "display_order": 1
    }
  ]
}
```

### 오류

- `400 BADREQUEST`: 템플릿 ID가 0 이하
- `401 INVALID_ACCESS_TOKEN`: 인증 실패
- `404 FINANCIAL_GOAL_TEMPLATE_NOT_FOUND`: 활성 기본 템플릿 없음
- `404 FINANCIAL_GOAL_RECOMMENDATION_NOT_FOUND`: 추천 기준 또는 추천 금액 없음

## 기존 DB 반영

`schema.sql`은 전체 스키마 초기화용이므로 기존 로컬 또는 배포 DB에 실행하지 않습니다. 아래 기능 전용 SQL은 테이블을 보존하고 같은 데이터를 다시 실행해도 갱신되도록 작성되어 있습니다.

```bash
mysql -u root -p azas < backend/src/main/resources/db/financial-goal-amount-recommendations.sql
```

실행 전 `financial_goal_template`에 기본 템플릿 ID `1, 2, 3, 4`가 존재하는지 확인합니다.

```sql
SELECT financial_goal_template_id, goal_name
FROM financial_goal_template
WHERE financial_goal_template_id IN (1, 2, 3, 4);
```
