# FinancialGoalListResponse

자녀 금융 목표 목록 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**child_id** | **number** | 자녀 ID | [default to undefined]
**financial_goals** | [**Array&lt;GoalResponse&gt;**](GoalResponse.md) | 진행 중·달성 목표 목록 | [default to undefined]
**total_count** | **number** | 목표 수 | [default to undefined]

## Example

```typescript
import { FinancialGoalListResponse } from './api';

const instance: FinancialGoalListResponse = {
    child_id,
    financial_goals,
    total_count,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
