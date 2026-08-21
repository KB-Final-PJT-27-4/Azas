# CreateTimeCapsuleRequest

타임캡슐 보관함 생성 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**financial_account_id** | **number** | 타임캡슐과 연결할 금융 계좌 ID | [default to undefined]
**release_date** | **string** | 공개 예정일. 생략하면 나중에 설정할 수 있습니다. | [optional] [default to undefined]

## Example

```typescript
import { CreateTimeCapsuleRequest } from './api';

const instance: CreateTimeCapsuleRequest = {
    financial_account_id,
    release_date,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
