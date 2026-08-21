# CreateTimeCapsuleMediaUploadUrlResponse

타임캡슐 대표 이미지 업로드 URL 발급 응답

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**expires_at** | **string** |  | [optional] [default to undefined]
**required_headers** | **{ [key: string]: string; }** |  | [optional] [default to undefined]
**time_capsule_entry_id** | **number** |  | [optional] [default to undefined]
**time_capsule_media_id** | **number** |  | [optional] [default to undefined]
**upload_url** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { CreateTimeCapsuleMediaUploadUrlResponse } from './api';

const instance: CreateTimeCapsuleMediaUploadUrlResponse = {
    expires_at,
    required_headers,
    time_capsule_entry_id,
    time_capsule_media_id,
    upload_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
