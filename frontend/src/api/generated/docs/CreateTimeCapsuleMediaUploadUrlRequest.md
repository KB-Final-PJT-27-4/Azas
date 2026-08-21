# CreateTimeCapsuleMediaUploadUrlRequest

타임캡슐 대표 이미지 업로드 URL 발급 요청

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**file_size** | **number** | 대표 이미지 파일 크기(byte, 최대 10MiB) | [default to undefined]
**mime_type** | **string** | 대표 이미지 MIME 타입 | [default to undefined]

## Example

```typescript
import { CreateTimeCapsuleMediaUploadUrlRequest } from './api';

const instance: CreateTimeCapsuleMediaUploadUrlRequest = {
    file_size,
    mime_type,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
