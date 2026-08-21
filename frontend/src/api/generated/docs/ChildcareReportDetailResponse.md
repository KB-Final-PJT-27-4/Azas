# ChildcareReportDetailResponse

월간 양육비 리포트 상세 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**calculated_at** | **string** |  | [optional] [default to undefined]
**child_id** | **number** |  | [optional] [default to undefined]
**monthly_flow** | [**Array&lt;MonthlyFlowItem&gt;**](MonthlyFlowItem.md) |  | [optional] [default to undefined]
**period** | [**Period**](Period.md) |  | [optional] [default to undefined]
**report_month** | **number** |  | [optional] [default to undefined]
**report_year** | **number** |  | [optional] [default to undefined]
**summary** | [**Summary**](Summary.md) |  | [optional] [default to undefined]

## Example

```typescript
import { ChildcareReportDetailResponse } from './api';

const instance: ChildcareReportDetailResponse = {
    calculated_at,
    child_id,
    monthly_flow,
    period,
    report_month,
    report_year,
    summary,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
