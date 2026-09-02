# MemberProfileResponse

내 회원 정보

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**birth_date** | **string** | 생년월일. 등록되지 않은 경우 null | [optional] [default to undefined]
**created_at** | **string** | 회원 생성 시각 | [default to undefined]
**email** | **string** | 소셜 제공자에서 받은 회원 이메일 | [default to undefined]
**member_id** | **number** | 회원 ID | [default to undefined]
**member_type** | **string** | 회원 유형 | [default to undefined]
**name** | **string** | 소셜 제공자에서 받은 회원 이름 | [default to undefined]
**phone_number** | **string** | 마스킹된 휴대폰 번호. 등록되지 않은 경우 null | [optional] [default to undefined]
**phone_verified** | **boolean** | 휴대폰 인증 완료 여부 | [default to undefined]
**phone_verified_at** | **string** | 휴대폰 인증 완료 시각. 미인증인 경우 null | [optional] [default to undefined]
**profile_image_url** | **string** | 프로필 이미지 URL. 등록되지 않은 경우 null | [optional] [default to undefined]
**social_accounts** | [**Array&lt;MemberSocialAccountResponse&gt;**](MemberSocialAccountResponse.md) | 연결된 소셜 계정 목록 | [default to undefined]
**status** | **string** | 회원 상태 | [default to undefined]
**updated_at** | **string** | 회원 수정 시각 | [default to undefined]

## Example

```typescript
import { MemberProfileResponse } from './api';

const instance: MemberProfileResponse = {
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
    social_accounts,
    status,
    updated_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
