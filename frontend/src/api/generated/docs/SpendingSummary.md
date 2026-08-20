# SpendingSummary


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_balance_hidden** | **boolean** | 자녀 화면에서 실제 계좌 잔액을 숨기는지 여부 | [optional] [default to undefined]
**account_id** | **number** |  | [optional] [default to undefined]
**budget_exceeded** | **boolean** |  | [optional] [default to undefined]
**child_usage_mode** | **string** |  | [optional] [default to undefined]
**current_month_spent_amount** | **number** |  | [optional] [default to undefined]
**display_available_amount** | **number** | 화면 상단에 표시할 사용 가능 금액 | [optional] [default to undefined]
**monthly_budget_amount** | **number** |  | [optional] [default to undefined]
**period** | **string** |  | [optional] [default to undefined]
**remaining_monthly_budget_amount** | **number** |  | [optional] [default to undefined]
**usage_rate** | **number** | 프로그레스바 표시용 0~100 사용률 | [optional] [default to undefined]

## Example

```typescript
import { SpendingSummary } from './api';

const instance: SpendingSummary = {
    account_balance_hidden,
    account_id,
    budget_exceeded,
    child_usage_mode,
    current_month_spent_amount,
    display_available_amount,
    monthly_budget_amount,
    period,
    remaining_monthly_budget_amount,
    usage_rate,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
