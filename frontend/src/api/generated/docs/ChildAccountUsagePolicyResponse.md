# ChildAccountUsagePolicyResponse

자녀 계좌 사용 관리 정책

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_id** | **number** | 금융 계좌 ID | [default to undefined]
**child_id** | **number** | 자녀 ID | [default to undefined]
**child_monthly_budget_amount** | **number** | 월간 사용 관리 기준 금액. 실제 금융기관 제한 금액이 아닙니다. | [optional] [default to undefined]
**child_usage_mode** | **string** | 자녀 계좌 사용 관리 모드 | [optional] [default to undefined]
**usage_policy_updated_at** | **string** | 사용 관리 정책 최종 변경 시각 | [optional] [default to undefined]

## Example

```typescript
import { ChildAccountUsagePolicyResponse } from './api';

const instance: ChildAccountUsagePolicyResponse = {
    account_id,
    child_id,
    child_monthly_budget_amount,
    child_usage_mode,
    usage_policy_updated_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
