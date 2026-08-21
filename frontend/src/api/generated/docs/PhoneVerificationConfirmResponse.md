# PhoneVerificationConfirmResponse

휴대폰 인증번호 확인 결과

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**phone_number** | **string** | 마스킹된 인증 휴대폰 번호 | [default to undefined]
**phone_verification_token** | **string** | 회원 정보에 휴대폰 번호를 반영할 때 사용하는 일회용 인증 토큰 | [default to undefined]
**token_expires_at** | **string** | 휴대폰 인증 토큰 만료 시각 | [default to undefined]
**verification_id** | **number** | 휴대폰 인증 요청 ID | [default to undefined]
**verified_at** | **string** | 인증 완료 시각 | [default to undefined]

## Example

```typescript
import { PhoneVerificationConfirmResponse } from './api';

const instance: PhoneVerificationConfirmResponse = {
    phone_number,
    phone_verification_token,
    token_expires_at,
    verification_id,
    verified_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
