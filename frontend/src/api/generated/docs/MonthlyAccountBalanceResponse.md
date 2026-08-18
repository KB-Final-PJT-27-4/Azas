# MonthlyAccountBalanceResponse

계좌 월별 잔액 정보

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**balance** | **number** | 해당 월 마지막 잔액. 스냅샷이 없으면 null | [optional] [default to undefined]
**change_amount** | **number** | 직전 달 마지막 잔액 대비 순변화액. 비교할 수 없으면 null | [optional] [default to undefined]
**month** | **string** | 조회 월 | [default to undefined]
**observed_at** | **string** | 해당 월 마지막 잔액 스냅샷 시각. 스냅샷이 없으면 null | [optional] [default to undefined]

## Example

```typescript
import { MonthlyAccountBalanceResponse } from './api';

const instance: MonthlyAccountBalanceResponse = {
    balance,
    change_amount,
    month,
    observed_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
