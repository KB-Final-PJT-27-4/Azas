# AccountTransactionDetailResponse

거래내역 상세 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_transaction_id** | **number** | 계좌 거래 ID | [default to undefined]
**amount** | **number** | 양수 거래 금액 | [default to undefined]
**balance_after** | **number** | 거래 직후 원장 계좌 잔액. 없으면 null | [optional] [default to undefined]
**deposit_account** | [**AccountTransactionPartyResponse**](AccountTransactionPartyResponse.md) |  | [default to undefined]
**direction** | **string** | 거래 원장 계좌 기준 입출금 구분 | [default to undefined]
**memo** | **string** | 거래 메모. 없으면 null | [optional] [default to undefined]
**occurred_at** | **string** | 거래 발생 시각(UTC) | [default to undefined]
**withdrawal_account** | [**AccountTransactionPartyResponse**](AccountTransactionPartyResponse.md) |  | [default to undefined]

## Example

```typescript
import { AccountTransactionDetailResponse } from './api';

const instance: AccountTransactionDetailResponse = {
    account_transaction_id,
    amount,
    balance_after,
    deposit_account,
    direction,
    memo,
    occurred_at,
    withdrawal_account,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
