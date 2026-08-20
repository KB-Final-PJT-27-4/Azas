# AssetReportDetailResponse

월간 자산 리포트 상세 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**asset_report_id** | **number** |  | [optional] [default to undefined]
**child_id** | **number** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**goal_summary** | [**Array&lt;GoalSummary&gt;**](GoalSummary.md) |  | [optional] [default to undefined]
**insight_items** | [**Array&lt;InsightItem&gt;**](InsightItem.md) |  | [optional] [default to undefined]
**period** | [**Period**](Period.md) |  | [optional] [default to undefined]
**report_month** | **number** |  | [optional] [default to undefined]
**report_year** | **number** |  | [optional] [default to undefined]
**summary** | [**Summary**](Summary.md) |  | [optional] [default to undefined]
**updated_at** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { AssetReportDetailResponse } from './api';

const instance: AssetReportDetailResponse = {
    asset_report_id,
    child_id,
    created_at,
    goal_summary,
    insight_items,
    period,
    report_month,
    report_year,
    summary,
    updated_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
