# OAuthLoginResponse

소셜 로그인 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**access_token** | **string** | Azas API 요청에 사용하는 Access Token | [default to undefined]
**expires_in** | **number** | Access Token 만료까지 남은 시간(초) | [default to undefined]
**is_new_member** | **boolean** | 이번 요청에서 새로 생성된 회원인지 여부 | [default to undefined]
**member** | [**OAuthLoginMemberResponse**](OAuthLoginMemberResponse.md) |  | [default to undefined]
**refresh_token** | **string** | Access Token 재발급에 사용하는 Refresh Token | [default to undefined]
**token_type** | **string** | 인증 방식 | [default to undefined]

## Example

```typescript
import { OAuthLoginResponse } from './api';

const instance: OAuthLoginResponse = {
    access_token,
    expires_in,
    is_new_member,
    member,
    refresh_token,
    token_type,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
