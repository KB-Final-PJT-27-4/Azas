# ChildAvailableAmountResponse

자녀 본인 월간 계좌 사용 관리 현황

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_id** | **number** | 대표 입출금 계좌 ID | [default to undefined]
**budget_exceeded** | **boolean** | 월간 관리 기준액 초과 여부. UNRESTRICTED이면 null입니다. | [optional] [default to undefined]
**calculated_at** | **string** | 사용 현황 계산 시각 | [default to undefined]
**child_id** | **number** | 자녀 ID | [default to undefined]
**child_monthly_budget_amount** | **number** | 월간 사용 관리 기준 금액. UNRESTRICTED이면 null이며 실제 결제 한도가 아닙니다. | [optional] [default to undefined]
**child_usage_mode** | **string** | 자녀 계좌 사용 관리 모드 | [default to undefined]
**current_month_spent_amount** | **number** | 현재 달 출금 거래 합계 | [default to undefined]
**period** | **string** | 사용액 집계 대상 월(UTC) | [default to undefined]
**remaining_guidance_amount** | **number** | 월간 기준액에서 사용액을 뺀 참고 금액. 0 미만이 되지 않으며 UNRESTRICTED이면 null입니다. | [optional] [default to undefined]

## Example

```typescript
import { ChildAvailableAmountResponse } from './api';

const instance: ChildAvailableAmountResponse = {
    account_id,
    budget_exceeded,
    calculated_at,
    child_id,
    child_monthly_budget_amount,
    child_usage_mode,
    current_month_spent_amount,
    period,
    remaining_guidance_amount,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
