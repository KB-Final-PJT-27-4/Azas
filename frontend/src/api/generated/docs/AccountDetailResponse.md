# AccountDetailResponse

계좌 상세 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**account_holder_name** | **string** | 예금주명 | [default to undefined]
**account_id** | **number** | 금융 계좌 ID | [default to undefined]
**account_name** | **string** | 계좌명 | [default to undefined]
**account_number** | **string** | 복호화된 전체 계좌번호 | [default to undefined]
**account_product_type** | **string** | 계좌 상품 유형 | [default to undefined]
**balance** | **number** | 현재 잔액 | [default to undefined]
**bank_name** | **string** | 은행명 | [default to undefined]
**owner_type** | **string** | 계좌 소유 유형 | [default to undefined]

## Example

```typescript
import { AccountDetailResponse } from './api';

const instance: AccountDetailResponse = {
    account_holder_name,
    account_id,
    account_name,
    account_number,
    account_product_type,
    balance,
    bank_name,
    owner_type,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
