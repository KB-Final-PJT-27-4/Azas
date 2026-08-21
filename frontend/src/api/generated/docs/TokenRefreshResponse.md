# TokenRefreshResponse

Access Token 재발급 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**access_token** | **string** | 새로 발급된 Access Token | [default to undefined]
**expires_in** | **number** | 새 Access Token의 유효시간(초) | [default to undefined]
**refresh_token** | **string** | 새로 발급된 Refresh Token | [default to undefined]
**token_type** | **string** | 인증 방식 | [default to undefined]

## Example

```typescript
import { TokenRefreshResponse } from './api';

const instance: TokenRefreshResponse = {
    access_token,
    expires_in,
    refresh_token,
    token_type,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
