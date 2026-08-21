# ChecklistItemListResponse

생애주기 체크리스트 목록 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**child_id** | **number** |  | [optional] [default to undefined]
**completed_count** | **number** |  | [optional] [default to undefined]
**items** | [**Array&lt;Item&gt;**](Item.md) |  | [optional] [default to undefined]
**lifecycle_stage** | **string** |  | [optional] [default to undefined]
**progress_percent** | **number** |  | [optional] [default to undefined]
**stage_completed** | **boolean** |  | [optional] [default to undefined]
**stage_description** | **string** |  | [optional] [default to undefined]
**stage_title** | **string** |  | [optional] [default to undefined]
**total_count** | **number** |  | [optional] [default to undefined]

## Example

```typescript
import { ChecklistItemListResponse } from './api';

const instance: ChecklistItemListResponse = {
    child_id,
    completed_count,
    items,
    lifecycle_stage,
    progress_percent,
    stage_completed,
    stage_description,
    stage_title,
    total_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
