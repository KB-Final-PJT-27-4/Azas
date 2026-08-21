# ParentAccountListResponse

부모 계좌 목록 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**accounts** | [**Array&lt;ParentAccountListItemResponse&gt;**](ParentAccountListItemResponse.md) | 부모 본인 명의 활성 계좌 목록 | [default to undefined]
**total_balance** | **number** | 부모 계좌 잔액 합계 | [default to undefined]
**total_count** | **number** | 연결 계좌 수 | [default to undefined]

## Example

```typescript
import { ParentAccountListResponse } from './api';

const instance: ParentAccountListResponse = {
    accounts,
    total_balance,
    total_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
