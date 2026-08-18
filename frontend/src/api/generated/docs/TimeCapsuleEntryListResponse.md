# TimeCapsuleEntryListResponse

타임캡슐 기록 목록 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**entries** | [**Array&lt;TimeCapsuleEntrySummaryResponse&gt;**](TimeCapsuleEntrySummaryResponse.md) |  | [optional] [default to undefined]
**time_capsule** | [**TimeCapsuleSummaryResponse**](TimeCapsuleSummaryResponse.md) |  | [optional] [default to undefined]
**total_count** | **number** |  | [optional] [default to undefined]

## Example

```typescript
import { TimeCapsuleEntryListResponse } from './api';

const instance: TimeCapsuleEntryListResponse = {
    entries,
    time_capsule,
    total_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
