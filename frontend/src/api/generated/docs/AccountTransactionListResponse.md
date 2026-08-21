# AccountTransactionListResponse

계좌 거래내역 목록 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_id** | **number** | 금융 계좌 ID | [default to undefined]
**has_next** | **boolean** | 다음 페이지 존재 여부 | [default to undefined]
**next_cursor** | **string** | 다음 페이지 커서. 마지막 페이지는 null | [optional] [default to undefined]
**transactions** | [**Array&lt;AccountTransactionItemResponse&gt;**](AccountTransactionItemResponse.md) | 거래내역 목록 | [default to undefined]

## Example

```typescript
import { AccountTransactionListResponse } from './api';

const instance: AccountTransactionListResponse = {
    account_id,
    has_next,
    next_cursor,
    transactions,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
