# OAuthLoginRequest

소셜 로그인 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**authorization_code** | **string** | 소셜 제공자가 발급한 일회용 인가 코드 | [default to undefined]
**redirect_uri** | **string** | 인가 코드 발급 요청에 사용한 Redirect URI | [default to undefined]

## Example

```typescript
import { OAuthLoginRequest } from './api';

const instance: OAuthLoginRequest = {
    authorization_code,
    redirect_uri,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
