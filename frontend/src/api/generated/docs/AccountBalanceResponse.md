# AccountBalanceResponse

계좌 최신 잔액 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_id** | **number** | 금융 계좌 ID | [default to undefined]
**balance** | **number** | 마지막 금융정보 동기화 기준 잔액 | [default to undefined]
**balance_updated_at** | **string** | 잔액 기준 시각 | [default to undefined]

## Example

```typescript
import { AccountBalanceResponse } from './api';

const instance: AccountBalanceResponse = {
    account_id,
    balance,
    balance_updated_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
