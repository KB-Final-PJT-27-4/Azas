# TransferCreateResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**amount** | **number** |  | [optional] [default to undefined]
**destination_account** | [**TransferAccountResponse**](TransferAccountResponse.md) |  | [optional] [default to undefined]
**financial_goal_id** | **number** |  | [optional] [default to undefined]
**financial_transfer_id** | **number** |  | [optional] [default to undefined]
**memo** | **string** |  | [optional] [default to undefined]
**requested_at** | **string** |  | [optional] [default to undefined]
**source_account** | [**TransferAccountResponse**](TransferAccountResponse.md) |  | [optional] [default to undefined]
**status** | **string** |  | [optional] [default to undefined]
**transfer_type** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { TransferCreateResponse } from './api';

const instance: TransferCreateResponse = {
    amount,
    destination_account,
    financial_goal_id,
    financial_transfer_id,
    memo,
    requested_at,
    source_account,
    status,
    transfer_type,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
