# NotificationListResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**has_more_new** | **boolean** |  | [optional] [default to undefined]
**has_next** | **boolean** |  | [optional] [default to undefined]
**items** | [**Array&lt;NotificationListItemResponse&gt;**](NotificationListItemResponse.md) |  | [optional] [default to undefined]
**next_cursor** | **number** |  | [optional] [default to undefined]
**poll_cursor** | **number** |  | [optional] [default to undefined]
**recommended_poll_interval_seconds** | **number** |  | [optional] [default to undefined]
**unread_count** | **number** |  | [optional] [default to undefined]

## Example

```typescript
import { NotificationListResponse } from './api';

const instance: NotificationListResponse = {
    has_more_new,
    has_next,
    items,
    next_cursor,
    poll_cursor,
    recommended_poll_interval_seconds,
    unread_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
