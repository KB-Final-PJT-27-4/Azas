# ChildDashboardResponse

자녀 본인 홈 대시보드

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**activity_summary** | [**ActivitySummary**](ActivitySummary.md) |  | [optional] [default to undefined]
**child** | [**ChildDashboardChildInfoResponse**](ChildDashboardChildInfoResponse.md) |  | [optional] [default to undefined]
**mission_summary** | [**MissionSummary**](MissionSummary.md) |  | [optional] [default to undefined]
**notification_summary** | [**ChildDashboardNotificationSummaryResponse**](ChildDashboardNotificationSummaryResponse.md) |  | [optional] [default to undefined]
**spending_summary** | [**SpendingSummary**](SpendingSummary.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ChildDashboardResponse } from './api';

const instance: ChildDashboardResponse = {
    activity_summary,
    child,
    mission_summary,
    notification_summary,
    spending_summary,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
