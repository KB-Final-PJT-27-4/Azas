# AccountBalanceHistoryResponse

계좌 월별 잔액 변화 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_id** | **number** | 금융 계좌 ID | [default to undefined]
**balance_history** | [**Array&lt;MonthlyAccountBalanceResponse&gt;**](MonthlyAccountBalanceResponse.md) | 월별 잔액 및 순변화 목록 | [default to undefined]
**end_month** | **string** | 조회 종료 월 | [default to undefined]
**months** | **number** | 조회 개월 수 | [default to undefined]
**start_month** | **string** | 조회 시작 월 | [default to undefined]

## Example

```typescript
import { AccountBalanceHistoryResponse } from './api';

const instance: AccountBalanceHistoryResponse = {
    account_id,
    balance_history,
    end_month,
    months,
    start_month,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
