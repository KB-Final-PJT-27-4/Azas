# AccountTransactionPartyResponse

거래 입금처 또는 출금처

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_name** | **string** | 계좌명 또는 거래 상대명. 확인할 수 없으면 null | [optional] [default to undefined]
**account_number** | **string** | 전체 계좌번호. 확인할 수 없으면 null | [optional] [default to undefined]
**bank_name** | **string** | 은행명. 확인할 수 없으면 null | [optional] [default to undefined]

## Example

```typescript
import { AccountTransactionPartyResponse } from './api';

const instance: AccountTransactionPartyResponse = {
    account_name,
    account_number,
    bank_name,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
