# MissionListResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**has_next** | **boolean** |  | [optional] [default to undefined]
**items** | [**Array&lt;MissionListItemResponse&gt;**](MissionListItemResponse.md) |  | [optional] [default to undefined]
**next_cursor** | **number** |  | [optional] [default to undefined]
**summary** | [**MissionListSummaryResponse**](MissionListSummaryResponse.md) |  | [optional] [default to undefined]

## Example

```typescript
import { MissionListResponse } from './api';

const instance: MissionListResponse = {
    has_next,
    items,
    next_cursor,
    summary,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
