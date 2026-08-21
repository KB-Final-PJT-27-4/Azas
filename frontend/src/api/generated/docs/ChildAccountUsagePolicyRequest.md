# ChildAccountUsagePolicyRequest

자녀 계좌 사용 관리 정책 설정 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**child_monthly_budget_amount** | **number** | 월간 사용 관리 기준 금액. CO_MANAGED일 때 필수이며 실제 금융기관 제한 금액이 아닙니다. | [optional] [default to undefined]
**child_usage_mode** | **string** | 자녀 계좌 사용 관리 모드 | [default to undefined]

## Example

```typescript
import { ChildAccountUsagePolicyRequest } from './api';

const instance: ChildAccountUsagePolicyRequest = {
    child_monthly_budget_amount,
    child_usage_mode,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
