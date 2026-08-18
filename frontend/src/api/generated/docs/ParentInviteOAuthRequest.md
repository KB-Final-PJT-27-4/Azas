# ParentInviteOAuthRequest

부모 초대코드 기반 소셜 로그인 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**authorization_code** | **string** | 소셜 제공자가 발급한 일회용 인가 코드 | [default to undefined]
**invite_token** | **string** | 가족 초대 링크에 포함된 원본 부모 초대 토큰 | [default to undefined]
**redirect_uri** | **string** | 인가 코드 발급 시 사용한 부모 초대 전용 Redirect URI | [default to undefined]

## Example

```typescript
import { ParentInviteOAuthRequest } from './api';

const instance: ParentInviteOAuthRequest = {
    authorization_code,
    invite_token,
    redirect_uri,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
