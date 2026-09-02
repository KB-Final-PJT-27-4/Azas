# PhoneVerificationSendResponse

휴대폰 인증번호 발송 결과

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**expires_at** | **string** | 인증번호 만료 시각 | [default to undefined]
**resend_available_at** | **string** | 재발송 가능 시각 | [default to undefined]
**verification_id** | **number** | 휴대폰 인증 요청 ID | [default to undefined]

## Example

```typescript
import { PhoneVerificationSendResponse } from './api';

const instance: PhoneVerificationSendResponse = {
    expires_at,
    resend_available_at,
    verification_id,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
