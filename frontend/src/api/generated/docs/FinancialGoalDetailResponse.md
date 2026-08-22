# FinancialGoalDetailResponse

자녀 금융 목표 상세 조회 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**achievement_rate** | **number** |  | [optional] [default to undefined]
**checkpoints** | [**Array&lt;CheckpointResponse&gt;**](CheckpointResponse.md) |  | [optional] [default to undefined]
**child_id** | **number** |  | [optional] [default to undefined]
**current_amount** | **number** |  | [optional] [default to undefined]
**financial_goal_id** | **number** |  | [optional] [default to undefined]
**financial_goal_template_id** | **number** |  | [optional] [default to undefined]
**icon_key** | **string** |  | [optional] [default to undefined]
**linked_account_count** | **number** |  | [optional] [default to undefined]
**linked_accounts** | [**Array&lt;FinancialGoalDetailLinkedAccountResponse&gt;**](FinancialGoalDetailLinkedAccountResponse.md) |  | [optional] [default to undefined]
**monthly_saving_amount** | **number** |  | [optional] [default to undefined]
**remaining_amount** | **number** |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**target_amount** | **number** |  | [optional] [default to undefined]
**target_date** | **string** |  | [optional] [default to undefined]
**title** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { FinancialGoalDetailResponse } from './api';

const instance: FinancialGoalDetailResponse = {
    achievement_rate,
    checkpoints,
    child_id,
    current_amount,
    financial_goal_id,
    financial_goal_template_id,
    icon_key,
    linked_account_count,
    linked_accounts,
    monthly_saving_amount,
    remaining_amount,
    status,
    target_amount,
    target_date,
    title,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
