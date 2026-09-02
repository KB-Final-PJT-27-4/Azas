# CreateAutoTransferScheduleRequest

자동이체 일정 등록 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**amount** | **number** | 회차별 이체 금액 | [default to undefined]
**child_id** | **number** | 받는 계좌 소유 자녀 ID | [optional] [default to undefined]
**destination_account_id** | **number** | 부모 또는 현재 자녀의 활성 입출금·적금 계좌 ID | [default to undefined]
**end_date** | **string** | 종료일(생략 가능) | [optional] [default to undefined]
**frequency** | **string** | 이체 주기 | [default to undefined]
**source_account_id** | **number** | 로그인 부모의 활성 입출금 계좌 ID | [default to undefined]
**start_date** | **string** | 최초 이체 가능일 | [default to undefined]
**transfer_day** | **number** | 매월 이체일(1~28) | [default to undefined]

## Example

```typescript
import { CreateAutoTransferScheduleRequest } from './api';

const instance: CreateAutoTransferScheduleRequest = {
    amount,
    child_id,
    destination_account_id,
    end_date,
    frequency,
    source_account_id,
    start_date,
    transfer_day,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
