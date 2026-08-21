# OAuthLoginMemberResponse

소셜 로그인 회원 정보

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**birth_date** | **string** | 생년월일. 등록되지 않은 경우 null | [optional] [default to undefined]
**created_at** | **string** | 회원 생성 시각 | [default to undefined]
**email** | **string** | 소셜 제공자에서 받은 회원 이메일 | [default to undefined]
**member_id** | **number** | 회원 ID | [default to undefined]
**member_type** | **string** | 회원 유형 | [default to undefined]
**name** | **string** | 소셜 제공자에서 받은 회원 이름 | [default to undefined]
**phone_number** | **string** | 휴대폰 번호. 인증되지 않은 경우 null | [optional] [default to undefined]
**phone_verified** | **boolean** | 휴대폰 인증 완료 여부 | [default to undefined]
**phone_verified_at** | **string** | 휴대폰 인증 완료 시각. 인증되지 않은 경우 null | [optional] [default to undefined]
**profile_image_url** | **string** | 프로필 이미지 URL. 등록되지 않은 경우 null | [optional] [default to undefined]

## Example

```typescript
import { OAuthLoginMemberResponse } from './api';

const instance: OAuthLoginMemberResponse = {
    birth_date,
    created_at,
    email,
    member_id,
    member_type,
    name,
    phone_number,
    phone_verified,
    phone_verified_at,
    profile_image_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
