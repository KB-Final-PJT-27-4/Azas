# AccountTransactionItemResponse

계좌 거래내역 목록 항목

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_transaction_id** | **number** | 계좌 거래 ID | [default to undefined]
**amount** | **number** | 양수 거래 금액 | [default to undefined]
**counterparty_name** | **string** | 거래 상대 표시명. 확인할 수 없으면 null | [optional] [default to undefined]
**direction** | **string** | 현재 계좌 기준 입출금 구분 | [default to undefined]
**occurred_at** | **string** | 거래 발생 시각(UTC) | [default to undefined]

## Example

```typescript
import { AccountTransactionItemResponse } from './api';

const instance: AccountTransactionItemResponse = {
    account_transaction_id,
    amount,
    counterparty_name,
    direction,
    occurred_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
