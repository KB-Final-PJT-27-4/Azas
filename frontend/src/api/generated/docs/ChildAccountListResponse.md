# ChildAccountListResponse

자녀 계좌 목록 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accounts** | [**Array&lt;ChildAccountListItemResponse&gt;**](ChildAccountListItemResponse.md) | 자녀 명의 활성 계좌 목록 | [default to undefined]
**child_id** | **number** | 조회 대상 자녀 ID | [default to undefined]
**total_balance** | **number** | 자녀 계좌 잔액 합계 | [default to undefined]
**total_count** | **number** | 연결 계좌 수 | [default to undefined]

## Example

```typescript
import { ChildAccountListResponse } from './api';

const instance: ChildAccountListResponse = {
    accounts,
    child_id,
    total_balance,
    total_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
