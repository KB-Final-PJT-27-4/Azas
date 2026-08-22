# DefaultApi

All URIs are relative to *http://localhost*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**acceptFamilyInvitationUsingPOST**](#acceptfamilyinvitationusingpost) | **POST** /api/v1/family-invitations/{invite_token}/accept | 초대코드 수락 및 계정 연결|
|[**cancelScheduleUsingDELETE**](#cancelscheduleusingdelete) | **DELETE** /api/v1/auto-transfer-schedules/{schedule_id} | 자동이체 일정 해지|
|[**completeMediaUploadUsingPOST**](#completemediauploadusingpost) | **POST** /api/v1/time-capsule-entries/{entry_id}/media/complete | TIMECAPSULE-8 타임캡슐 단일 이미지 업로드 완료|
|[**confirmVerificationCodeUsingPOST**](#confirmverificationcodeusingpost) | **POST** /api/v1/members/me/phone-verifications/{verification_id}/confirm | 휴대폰 SMS 인증번호 확인|
|[**createAllowanceRequestUsingPOST**](#createallowancerequestusingpost) | **POST** /api/v1/children/me/allowance-requests | 자녀 본인 용돈 요청|
|[**createChildFamilyInvitationUsingPOST**](#createchildfamilyinvitationusingpost) | **POST** /api/v1/children/{child_id}/family-invitations | 자녀 계정 연결 초대코드 발급|
|[**createChildUsingPOST**](#createchildusingpost) | **POST** /api/v1/children | 자녀 프로필 등록|
|[**createGoalUsingPOST**](#creategoalusingpost) | **POST** /api/v1/children/{child_id}/financial-goals | GOAL-2 자녀 금융 목표 생성|
|[**createMediaUploadUrlUsingPOST**](#createmediauploadurlusingpost) | **POST** /api/v1/time-capsule-entries/{entry_id}/media/upload-url | TIMECAPSULE-7 타임캡슐 대표 이미지 업로드 URL 발급|
|[**createMissionUsingPOST**](#createmissionusingpost) | **POST** /api/v1/children/{child_id}/missions | 미션 생성|
|[**createParentFamilyInvitationUsingPOST**](#createparentfamilyinvitationusingpost) | **POST** /api/v1/family-invitations | 보호자 초대코드 발급|
|[**createScheduleUsingPOST**](#createscheduleusingpost) | **POST** /api/v1/auto-transfer-schedules | 자동이체 일정 등록|
|[**createTimeCapsuleEntryUsingPOST**](#createtimecapsuleentryusingpost) | **POST** /api/v1/time-capsules/{time_capsule_id}/entries | TIMECAPSULE-5 타임캡슐 기록 생성|
|[**createTimeCapsuleUsingPOST**](#createtimecapsuleusingpost) | **POST** /api/v1/children/{child_id}/time-capsules | TIMECAPSULE-1 타임캡슐 보관함 생성|
|[**createTransferUsingPOST**](#createtransferusingpost) | **POST** /api/v1/transfers | TRANSFER-1 목표 계좌로 이체 요청(수동)|
|[**deleteChildUsingDELETE**](#deletechildusingdelete) | **DELETE** /api/v1/children/{childId} | deleteChild|
|[**deleteGoalUsingDELETE**](#deletegoalusingdelete) | **DELETE** /api/v1/financial-goals/{financial_goal_id} | GOAL-6 자녀 금융 목표 삭제|
|[**deleteTimeCapsuleEntryUsingDELETE**](#deletetimecapsuleentryusingdelete) | **DELETE** /api/v1/time-capsule-entries/{entry_id} | TIMECAPSULE-13 타임캡슐 엔트리 삭제|
|[**deleteTimeCapsuleUsingDELETE**](#deletetimecapsuleusingdelete) | **DELETE** /api/v1/time-capsules/{time_capsule_id} | TIMECAPSULE-6 타임캡슐 보관함 삭제|
|[**estimateMaturityUsingPOST**](#estimatematurityusingpost) | **POST** /api/v1/financial-products/{financial_product_id}/maturity-estimate | PRODUCT-5 예상 만기금액 계산|
|[**getAccountDetailUsingGET**](#getaccountdetailusingget) | **GET** /api/v1/accounts/{account_id} | ACCOUNT-15 계좌 상세 조회|
|[**getAllowanceRequestDetailUsingGET**](#getallowancerequestdetailusingget) | **GET** /api/v1/allowance-requests/{allowance_request_id} | 용돈 요청 상세 조회|
|[**getAllowanceRequestsUsingGET**](#getallowancerequestsusingget) | **GET** /api/v1/children/{child_id}/allowance-requests | 자녀 용돈 요청 목록 조회|
|[**getAmountRecommendationsUsingGET**](#getamountrecommendationsusingget) | **GET** /api/v1/financial-goal-templates/{financial_goal_template_id}/amount-recommendations | GOAL-2 목표 금액 추천 조회|
|[**getAssetReportDetailUsingGET**](#getassetreportdetailusingget) | **GET** /api/v1/children/{child_id}/asset-reports/{year}/{month} | 월간 자산 리포트 상세 조회|
|[**getAssetReportsUsingGET**](#getassetreportsusingget) | **GET** /api/v1/children/{child_id}/asset-reports | 자산 리포트 월 목록 조회|
|[**getBalanceHistoryUsingGET**](#getbalancehistoryusingget) | **GET** /api/v1/accounts/{account_id}/balance-history | ACCOUNT-17 계좌 월별 잔액 변화 조회|
|[**getBookmarksUsingGET**](#getbookmarksusingget) | **GET** /api/v1/children/{child_id}/financial-products/bookmarks | PRODUCT-2 자녀별 관심상품 목록 조회|
|[**getChecklistItemsUsingGET**](#getchecklistitemsusingget) | **GET** /api/v1/children/{child_id}/checklist-items | 생애주기 체크리스트 조회|
|[**getChildAccountsUsingGET**](#getchildaccountsusingget) | **GET** /api/v1/children/{child_id}/accounts | ACCOUNT-14 자녀 계좌 목록 조회|
|[**getChildMemberLinkUsingGET**](#getchildmemberlinkusingget) | **GET** /api/v1/children/{child_id}/member-link | 아이 계정 연결 상태 조회|
|[**getChildTransfersUsingGET**](#getchildtransfersusingget) | **GET** /api/v1/children/{child_id}/transfers | TRANSFER-2 자녀 목표별 서비스 이체 내역 조회|
|[**getChildUsingGET**](#getchildusingget) | **GET** /api/v1/children/{childId} | 자녀 상세 조회|
|[**getChildrenUsingGET**](#getchildrenusingget) | **GET** /api/v1/children | 자녀 목록 조회|
|[**getCurrentMonthUsageUsingGET**](#getcurrentmonthusageusingget) | **GET** /api/v1/children/me/available-amount | ACCOUNT-19 자녀 본인 월간 사용 현황 조회|
|[**getDashboardUsingGET**](#getdashboardusingget) | **GET** /api/v1/children/me/dashboard | 자녀 본인 홈 대시보드 조회|
|[**getDashboardUsingGET1**](#getdashboardusingget1) | **GET** /api/v1/children/{childId}/dashboard | 부모용 자녀 홈 대시보드 조회|
|[**getDiscoveredAccountsUsingGET**](#getdiscoveredaccountsusingget) | **GET** /api/v1/accounts/discovered | ACCOUNT-2 연결 가능한 Mock 계좌 목록 조회|
|[**getFamilyInvitationInfoUsingGET**](#getfamilyinvitationinfousingget) | **GET** /api/v1/family-invitations/{invite_token} | 초대코드 정보 조회|
|[**getFamilyMembersUsingGET**](#getfamilymembersusingget) | **GET** /api/v1/children/{child_id}/family-members | 함께 관리하는 보호자 목록 조회|
|[**getGoalUsingGET**](#getgoalusingget) | **GET** /api/v1/financial-goals/{financial_goal_id} | GOAL-4 자녀 금융 목표 상세 조회|
|[**getGoalsUsingGET**](#getgoalsusingget) | **GET** /api/v1/children/{child_id}/financial-goals | GOAL-3 자녀 금융 목표 목록 조회|
|[**getLatestBalanceUsingGET**](#getlatestbalanceusingget) | **GET** /api/v1/accounts/{account_id}/balance | ACCOUNT-16 계좌 최신 잔액 조회|
|[**getMemberTransfersUsingGET**](#getmembertransfersusingget) | **GET** /api/v1/members/me/transfers | TRANSFER-3 이체 처리 결과 전체 조회|
|[**getMissionDetailUsingGET**](#getmissiondetailusingget) | **GET** /api/v1/missions/{mission_id} | 미션 상세 조회|
|[**getMissionsUsingGET**](#getmissionsusingget) | **GET** /api/v1/children/{child_id}/missions | 미션 목록 조회|
|[**getMyAccountsUsingGET**](#getmyaccountsusingget) | **GET** /api/v1/members/me/accounts | ACCOUNT-13 부모 계좌 목록 조회|
|[**getMyProfileUsingGET**](#getmyprofileusingget) | **GET** /api/v1/members/me | 내 회원 정보 조회|
|[**getNotificationPreferencesUsingGET**](#getnotificationpreferencesusingget) | **GET** /api/v1/notification-preferences | 알림 유형별 수신 설정 조회|
|[**getNotificationsUsingGET**](#getnotificationsusingget) | **GET** /api/v1/notifications | 알림 목록 조회 및 신규 알림 폴링|
|[**getPermissionUsingGET**](#getpermissionusingget) | **GET** /api/v1/children/{child_id}/feature-permissions | 자녀 이용 권한 조회|
|[**getPregnancyStatusUsingGET**](#getpregnancystatususingget) | **GET** /api/v1/children/{child_id}/pregnancy-status | 임신 주차 및 캐릭터 조회|
|[**getProductDetailUsingGET**](#getproductdetailusingget) | **GET** /api/v1/financial-products/{financial_product_id} | PRODUCT-4 금융상품 상세 조회|
|[**getProductsUsingGET**](#getproductsusingget) | **GET** /api/v1/financial-products | PRODUCT-1 KB 금융상품 목록 조회|
|[**getReportUsingGET**](#getreportusingget) | **GET** /api/v1/children/{child_id}/childcare-reports/{year}/{month} | 월간 양육비 리포트 상세 조회|
|[**getScheduleDetailUsingGET**](#getscheduledetailusingget) | **GET** /api/v1/auto-transfer-schedules/{schedule_id} | 자동이체 일정 상세 조회|
|[**getSchedulesUsingGET**](#getschedulesusingget) | **GET** /api/v1/children/{child_id}/auto-transfer-schedules | 자녀 자동이체 일정 목록 조회|
|[**getTemplatesUsingGET**](#gettemplatesusingget) | **GET** /api/v1/financial-goal-templates | GOAL-1 금융 목표 템플릿 조회|
|[**getTimeCapsuleEntriesUsingGET**](#gettimecapsuleentriesusingget) | **GET** /api/v1/time-capsules/{time_capsule_id}/entries | TIMECAPSULE-4 타임캡슐 엔트리 목록 조회|
|[**getTimeCapsuleEntryUsingGET**](#gettimecapsuleentryusingget) | **GET** /api/v1/time-capsule-entries/{entry_id} | TIMECAPSULE-14 공개된 타임캡슐 엔트리 상세 조회|
|[**getTimeCapsulesUsingGET**](#gettimecapsulesusingget) | **GET** /api/v1/children/{child_id}/time-capsules | TIMECAPSULE-2 타임캡슐 보관함 목록 조회|
|[**getTransactionDetailUsingGET**](#gettransactiondetailusingget) | **GET** /api/v1/account-transactions/{account_transaction_id} | ACCOUNT-24 거래내역 상세 조회|
|[**getTransactionsUsingGET**](#gettransactionsusingget) | **GET** /api/v1/accounts/{account_id}/transactions | ACCOUNT-22 계좌 거래내역 목록 조회|
|[**getTransferUsingGET**](#gettransferusingget) | **GET** /api/v1/transfers/{transfer_id} | TRANSFER-4 이체 처리 결과 상세 조회|
|[**getUnreadCountUsingGET**](#getunreadcountusingget) | **GET** /api/v1/notifications/unread-count | 읽지 않은 알림 수 조회|
|[**getUsagePolicyUsingGET**](#getusagepolicyusingget) | **GET** /api/v1/accounts/{account_id}/child-usage-policy | ACCOUNT-12 입출금 계좌 자녀 사용 관리 정책 조회|
|[**linkUsingPOST**](#linkusingpost) | **POST** /api/v1/accounts/link | ACCOUNT-3 선택한 Mock 계좌 연결|
|[**loginUsingPOST**](#loginusingpost) | **POST** /api/v1/auth/oauth/{provider} | 소셜 로그인/회원가입|
|[**loginWithChildInviteUsingPOST**](#loginwithchildinviteusingpost) | **POST** /api/v1/auth/oauth/{provider}/child-invite | 자녀 초대코드 기반 소셜 회원가입/로그인|
|[**loginWithParentInviteUsingPOST**](#loginwithparentinviteusingpost) | **POST** /api/v1/auth/oauth/{provider}/parent-invite | 부모 초대코드 기반 소셜 회원가입/로그인|
|[**logoutUsingPOST**](#logoutusingpost) | **POST** /api/v1/auth/logout | 로그아웃|
|[**openUsingPOST**](#openusingpost) | **POST** /api/v1/accounts/open | ACCOUNT-4 KB 금융상품 기반 Mock 계좌 개설|
|[**readAllNotificationsUsingPATCH**](#readallnotificationsusingpatch) | **PATCH** /api/v1/notifications/read-all | 알림 전체 읽음 처리|
|[**readNotificationUsingPATCH**](#readnotificationusingpatch) | **PATCH** /api/v1/notifications/{notification_id}/read | 알림 한 건 읽음 처리|
|[**refreshUsingPOST**](#refreshusingpost) | **POST** /api/v1/auth/token/refresh | Access Token 재발급|
|[**registerUsingPOST**](#registerusingpost) | **POST** /api/v1/push-devices | 푸시 기기 등록 또는 토큰 갱신|
|[**retryUsingPOST**](#retryusingpost) | **POST** /api/v1/auto-transfer-schedules/{schedule_id}/retry | 자동이체 실패 회차 수동 재시도|
|[**sealTimeCapsuleEntryUsingPATCH**](#sealtimecapsuleentryusingpatch) | **PATCH** /api/v1/time-capsule-entries/{entry_id}/seal | TIMECAPSULE-15 타임캡슐 엔트리 봉인|
|[**sendVerificationCodeUsingPOST**](#sendverificationcodeusingpost) | **POST** /api/v1/members/me/phone-verifications | 휴대폰 SMS 인증번호 발송|
|[**setPrimaryAccountUsingPATCH**](#setprimaryaccountusingpatch) | **PATCH** /api/v1/accounts/{account_id}/primary | ACCOUNT-23 대표 계좌 설정|
|[**unlinkAccountUsingDELETE**](#unlinkaccountusingdelete) | **DELETE** /api/v1/accounts/{account_id} | ACCOUNT-18 계좌 서비스 연결 해제|
|[**unregisterUsingDELETE**](#unregisterusingdelete) | **DELETE** /api/v1/push-devices/{push_device_id} | 푸시 기기 해제|
|[**updateAllowanceRequestStatusUsingPATCH**](#updateallowancerequeststatususingpatch) | **PATCH** /api/v1/allowance-requests/{allowance_request_id} | 용돈 요청 상태 변경|
|[**updateBookmarkUsingPUT**](#updatebookmarkusingput) | **PUT** /api/v1/children/{child_id}/financial-products/{financial_product_id}/bookmark | PRODUCT-3 자녀별 관심상품 저장·해제|
|[**updateChecklistItemCompletionUsingPATCH**](#updatechecklistitemcompletionusingpatch) | **PATCH** /api/v1/checklist-items/{checklist_item_id}/completion | updateChecklistItemCompletion|
|[**updateChildUsingPATCH**](#updatechildusingpatch) | **PATCH** /api/v1/children/{childId} | updateChild|
|[**updateGoalUsingPATCH**](#updategoalusingpatch) | **PATCH** /api/v1/financial-goals/{financial_goal_id} | GOAL-5 자녀 금융 목표 수정|
|[**updateMissionStatusUsingPATCH**](#updatemissionstatususingpatch) | **PATCH** /api/v1/missions/{mission_id} | 미션 상태 변경|
|[**updateMyProfileUsingPATCH**](#updatemyprofileusingpatch) | **PATCH** /api/v1/members/me | 내 회원 정보 수정|
|[**updateNotificationPreferencesUsingPUT**](#updatenotificationpreferencesusingput) | **PUT** /api/v1/notification-preferences | 알림 유형별 수신 설정 저장|
|[**updatePermissionUsingPATCH**](#updatepermissionusingpatch) | **PATCH** /api/v1/children/{child_id}/feature-permissions | 자녀 이용 권한 수정|
|[**updateScheduleUsingPATCH**](#updatescheduleusingpatch) | **PATCH** /api/v1/auto-transfer-schedules/{schedule_id} | 자동이체 일정 수정·일시정지·재개|
|[**updateTimeCapsuleReleaseDateUsingPATCH**](#updatetimecapsulereleasedateusingpatch) | **PATCH** /api/v1/time-capsules/{time_capsule_id}/release-date | TIMECAPSULE-16 타임캡슐 공개일 설정·변경|
|[**updateUsagePolicyUsingPATCH**](#updateusagepolicyusingpatch) | **PATCH** /api/v1/accounts/{account_id}/child-usage-policy | ACCOUNT-11 입출금 계좌 자녀 사용 관리 정책 설정|
|[**withdrawMyMembershipUsingDELETE**](#withdrawmymembershipusingdelete) | **DELETE** /api/v1/members/me | 회원 탈퇴|

# **acceptFamilyInvitationUsingPOST**
> FamilyInvitationAcceptResponse acceptFamilyInvitationUsingPOST()


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FamilyInvitationAcceptRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let inviteToken: string; //invite_token (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let request: FamilyInvitationAcceptRequest; //request (optional)

const { status, data } = await apiInstance.acceptFamilyInvitationUsingPOST(
    inviteToken,
    authorization,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FamilyInvitationAcceptRequest**| request | |
| **inviteToken** | [**string**] | invite_token | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FamilyInvitationAcceptResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **cancelScheduleUsingDELETE**
> cancelScheduleUsingDELETE()

자동이체 일정을 삭제하지 않고 CANCELED 상태로 변경합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let scheduleId: number; //schedule_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.cancelScheduleUsingDELETE(
    scheduleId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **scheduleId** | [**number**] | schedule_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 자동이체 일정 해지 성공 |  -  |
|**400** | 잘못된 요청 |  -  |
|**401** | 인증 실패 |  -  |
|**403** | 일정 해지 권한 없음 |  -  |
|**404** | 자동이체 일정 없음 |  -  |
|**409** | 해지할 수 없는 일정 상태 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **completeMediaUploadUsingPOST**
> CompleteTimeCapsuleMediaUploadResponse completeMediaUploadUsingPOST(request)

작성자 본인의 DRAFT 엔트리에 속한 단일 이미지의 S3 객체 존재 여부, MIME 타입, 파일 크기를 검증한 뒤 활성화합니다. 이미 활성화된 동일 이미지 요청은 멱등 성공으로 처리하며 엔트리 봉인은 별도 API에서 수행합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CompleteTimeCapsuleMediaUploadRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let entryId: number; //타임캡슐 엔트리 ID (default to undefined)
let request: CompleteTimeCapsuleMediaUploadRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.completeMediaUploadUsingPOST(
    entryId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CompleteTimeCapsuleMediaUploadRequest**| request | |
| **entryId** | [**number**] | 타임캡슐 엔트리 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**CompleteTimeCapsuleMediaUploadResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **confirmVerificationCodeUsingPOST**
> PhoneVerificationConfirmResponse confirmVerificationCodeUsingPOST(request)

발급된 인증 요청 ID와 인증번호를 확인합니다. 성공하면 회원정보 수정에 사용할 수 있는 일회용 인증 토큰을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    PhoneVerificationConfirmRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let verificationId: number; //verification_id (default to undefined)
let request: PhoneVerificationConfirmRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.confirmVerificationCodeUsingPOST(
    verificationId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **PhoneVerificationConfirmRequest**| request | |
| **verificationId** | [**number**] | verification_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**PhoneVerificationConfirmResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 휴대폰 인증번호 확인 성공 |  -  |
|**400** | 인증 요청을 사용할 수 없거나 인증번호가 일치하지 않음 |  -  |
|**401** | Access Token 누락·만료 또는 유효하지 않음 |  -  |
|**429** | 인증번호 입력 가능 횟수 초과 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createAllowanceRequestUsingPOST**
> AllowancePlanRequestResponse createAllowanceRequestUsingPOST(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateAllowanceRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: CreateAllowanceRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createAllowanceRequestUsingPOST(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateAllowanceRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AllowancePlanRequestResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createChildFamilyInvitationUsingPOST**
> FamilyInvitationCreateResponse createChildFamilyInvitationUsingPOST()


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FamilyInvitationCreateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let request: FamilyInvitationCreateRequest; //request (optional)

const { status, data } = await apiInstance.createChildFamilyInvitationUsingPOST(
    childId,
    authorization,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FamilyInvitationCreateRequest**| request | |
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FamilyInvitationCreateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createChildUsingPOST**
> ChildResponse createChildUsingPOST(request)

부모가 자녀 프로필을 등록합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChildCreateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: ChildCreateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createChildUsingPOST(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChildCreateRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createGoalUsingPOST**
> FinancialGoalCreateResponse createGoalUsingPOST(request)

부모가 자녀의 금융 목표를 생성하고 요청 부모 본인 또는 대상 자녀의 활성 적금 계좌를 하나 이상 연결합니다. 하나의 적금 계좌는 하나의 활성 목표에만 연결할 수 있습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FinancialGoalCreateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let request: FinancialGoalCreateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createGoalUsingPOST(
    childId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FinancialGoalCreateRequest**| request | |
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalCreateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**201** | 자녀 금융 목표 생성 성공 |  -  |
|**400** | 목표 생성 요청 값 오류 |  -  |
|**401** | 유효하지 않은 Access Token |  -  |
|**403** | 부모 권한 또는 자녀 접근 권한 없음 |  -  |
|**404** | 자녀·목표 템플릿·적금 계좌를 찾을 수 없음 |  -  |
|**409** | 선택한 적금 계좌가 이미 다른 목표에 연결됨 |  -  |
|**422** | 목표 연결이 불가능한 계좌 또는 이미 달성된 목표 금액 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createMediaUploadUrlUsingPOST**
> CreateTimeCapsuleMediaUploadUrlResponse createMediaUploadUrlUsingPOST(request)

작성자 본인의 DRAFT 엔트리에 JPEG·PNG·WebP 대표 이미지 한 장을 업로드할 수 있는 15분 유효 S3 Presigned PUT URL을 발급합니다. 별도 썸네일 파일은 생성하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateTimeCapsuleMediaUploadUrlRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let entryId: number; //타임캡슐 엔트리 ID (default to undefined)
let request: CreateTimeCapsuleMediaUploadUrlRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createMediaUploadUrlUsingPOST(
    entryId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateTimeCapsuleMediaUploadUrlRequest**| request | |
| **entryId** | [**number**] | 타임캡슐 엔트리 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**CreateTimeCapsuleMediaUploadUrlResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createMissionUsingPOST**
> MissionCreateResponse createMissionUsingPOST(request)

연결된 부모가 자녀에게 보상형 용돈 미션을 생성합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateMissionRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let request: CreateMissionRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createMissionUsingPOST(
    childId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateMissionRequest**| request | |
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**MissionCreateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**201** | 미션 생성 성공 |  -  |
|**400** | 미션 입력값 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 부모 권한 없음 |  -  |
|**404** | 자녀 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createParentFamilyInvitationUsingPOST**
> FamilyInvitationCreateResponse createParentFamilyInvitationUsingPOST()


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FamilyInvitationCreateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)
let request: FamilyInvitationCreateRequest; //request (optional)

const { status, data } = await apiInstance.createParentFamilyInvitationUsingPOST(
    authorization,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FamilyInvitationCreateRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FamilyInvitationCreateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createScheduleUsingPOST**
> AutoTransferScheduleResponse createScheduleUsingPOST(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateAutoTransferScheduleRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: CreateAutoTransferScheduleRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)
let idempotencyKey: string; //Idempotency-Key (optional) (default to undefined)

const { status, data } = await apiInstance.createScheduleUsingPOST(
    request,
    authorization,
    idempotencyKey
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateAutoTransferScheduleRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **idempotencyKey** | [**string**] | Idempotency-Key | (optional) defaults to undefined|


### Return type

**AutoTransferScheduleResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createTimeCapsuleEntryUsingPOST**
> CreateTimeCapsuleEntryResponse createTimeCapsuleEntryUsingPOST(request)

부모가 타임캡슐 계좌의 입금 거래와 제목·편지를 선택해 DRAFT 기록을 생성합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateTimeCapsuleEntryRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let timeCapsuleId: number; //타임캡슐 보관함 ID (default to undefined)
let request: CreateTimeCapsuleEntryRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createTimeCapsuleEntryUsingPOST(
    timeCapsuleId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateTimeCapsuleEntryRequest**| request | |
| **timeCapsuleId** | [**number**] | 타임캡슐 보관함 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**CreateTimeCapsuleEntryResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createTimeCapsuleUsingPOST**
> CreateTimeCapsuleResponse createTimeCapsuleUsingPOST(request)

부모가 접근 가능한 부모 또는 자녀의 활성 입출금·적금 계좌를 특정 자녀의 타임캡슐과 연결합니다. 보관함 제목은 계좌명으로 자동 생성되며 공개 날짜는 선택 입력입니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateTimeCapsuleRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //타임캡슐 대상 자녀 ID (default to undefined)
let request: CreateTimeCapsuleRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createTimeCapsuleUsingPOST(
    childId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateTimeCapsuleRequest**| request | |
| **childId** | [**number**] | 타임캡슐 대상 자녀 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**CreateTimeCapsuleResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **createTransferUsingPOST**
> TransferCreateResponse createTransferUsingPOST(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    CreateTransferRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let idempotencyKey: string; //Idempotency-Key (default to undefined)
let request: CreateTransferRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.createTransferUsingPOST(
    idempotencyKey,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **CreateTransferRequest**| request | |
| **idempotencyKey** | [**string**] | Idempotency-Key | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TransferCreateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteChildUsingDELETE**
> deleteChildUsingDELETE()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //childId (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.deleteChildUsingDELETE(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | childId | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteGoalUsingDELETE**
> deleteGoalUsingDELETE()

부모가 접근 가능한 자녀 금융 목표를 보관하고 연결된 모든 적금 계좌를 해제합니다. 계좌, 잔액, 거래, 체크포인트와 타임캡슐 데이터는 보존합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialGoalId: number; //financial_goal_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.deleteGoalUsingDELETE(
    financialGoalId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **financialGoalId** | [**number**] | financial_goal_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 자녀 금융 목표 삭제 성공 |  -  |
|**400** | 금융 목표 ID 오류 |  -  |
|**401** | 유효하지 않은 Access Token |  -  |
|**403** | 부모 권한 없음 |  -  |
|**404** | 금융 목표를 찾을 수 없음 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteTimeCapsuleEntryUsingDELETE**
> deleteTimeCapsuleEntryUsingDELETE()

작성자 본인의 공개 전 DRAFT 또는 SEALED 엔트리를 삭제합니다. DB 엔트리와 미디어는 삭제 상태로 변경하고 S3 원본 객체를 제거합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let entryId: number; //타임캡슐 엔트리 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.deleteTimeCapsuleEntryUsingDELETE(
    entryId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **entryId** | [**number**] | 타임캡슐 엔트리 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteTimeCapsuleUsingDELETE**
> deleteTimeCapsuleUsingDELETE()

접근 가능한 부모 또는 보호자가 보관함과 내부 엔트리·미디어 및 원본 저장 객체를 복구할 수 없도록 영구 삭제합니다. 자녀·계좌·거래·목표 데이터는 유지됩니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let timeCapsuleId: number; //타임캡슐 보관함 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.deleteTimeCapsuleUsingDELETE(
    timeCapsuleId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **timeCapsuleId** | [**number**] | 타임캡슐 보관함 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **estimateMaturityUsingPOST**
> FinancialProductMaturityEstimateResponse estimateMaturityUsingPOST(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FinancialProductMaturityEstimateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialProductId: number; //financial_product_id (default to undefined)
let request: FinancialProductMaturityEstimateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.estimateMaturityUsingPOST(
    financialProductId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FinancialProductMaturityEstimateRequest**| request | |
| **financialProductId** | [**number**] | financial_product_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialProductMaturityEstimateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAccountDetailUsingGET**
> AccountDetailResponse getAccountDetailUsingGET()

부모 명의 활성 Mock 계좌는 소유 부모 본인만 조회할 수 있습니다. 자녀 명의 활성 Mock 계좌는 해당 자녀와 연결된 부모 또는 자녀 본인이 조회할 수 있습니다. 계좌 상세 화면에 필요한 은행명, 계좌명, 복호화된 전체 계좌번호, 예금주명, 상품 유형과 현재 잔액을 반환합니다. 최근 거래내역은 ACCOUNT-22를 별도로 호출합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getAccountDetailUsingGET(
    accountId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AccountDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 계좌 상세 조회 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 금융 계좌에 접근할 권한이 없음 |  -  |
|**404** | 계좌가 없거나 서비스 연결이 해제됨 |  -  |
|**500** | 계좌번호 복호화 실패 등 서버 내부 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllowanceRequestDetailUsingGET**
> AllowanceRequestDetailResponse getAllowanceRequestDetailUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let allowanceRequestId: number; //allowance_request_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getAllowanceRequestDetailUsingGET(
    allowanceRequestId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **allowanceRequestId** | [**number**] | allowance_request_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AllowanceRequestDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAllowanceRequestsUsingGET**
> AllowanceRequestListResponse getAllowanceRequestsUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let size: string; //size (optional) (default to undefined)
let status: string; //status (optional) (default to undefined)

const { status, data } = await apiInstance.getAllowanceRequestsUsingGET(
    childId,
    authorization,
    cursor,
    size,
    status
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **size** | [**string**] | size | (optional) defaults to undefined|
| **status** | [**string**] | status | (optional) defaults to undefined|


### Return type

**AllowanceRequestListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAmountRecommendationsUsingGET**
> FinancialGoalAmountRecommendationResponse getAmountRecommendationsUsingGET()

선택한 기본 금융 목표에 대해 공공 통계를 참고하여 서비스가 구성한 4단계 추천 금액과 산정 근거를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialGoalTemplateId: number; //financial_goal_template_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getAmountRecommendationsUsingGET(
    financialGoalTemplateId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **financialGoalTemplateId** | [**number**] | financial_goal_template_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalAmountRecommendationResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 목표 금액 추천 조회 성공 |  -  |
|**400** | 올바르지 않은 목표 템플릿 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**404** | 목표 템플릿 또는 추천 금액 없음 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAssetReportDetailUsingGET**
> AssetReportDetailResponse getAssetReportDetailUsingGET()

연결된 부모가 특정 연월의 자산 리포트, 목표별 달성률, 연결 적금 및 인사이트를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let month: number; //month (default to undefined)
let year: number; //year (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getAssetReportDetailUsingGET(
    childId,
    month,
    year,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **month** | [**number**] | month | defaults to undefined|
| **year** | [**number**] | year | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AssetReportDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 월간 자산 리포트 상세 조회 성공 |  -  |
|**400** | 잘못된 자녀 ID, 연도 또는 월 |  -  |
|**401** | Access Token 누락·만료·위조 |  -  |
|**403** | 해당 자녀의 부모 권한 없음 |  -  |
|**404** | 자녀 또는 해당 월 자산 리포트 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAssetReportsUsingGET**
> AssetReportListResponse getAssetReportsUsingGET()

연결된 부모가 자녀의 월별 자산 리포트를 최신 월순으로 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)
let year: number; //year (optional) (default to undefined)

const { status, data } = await apiInstance.getAssetReportsUsingGET(
    childId,
    authorization,
    cursor,
    size,
    year
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|
| **year** | [**number**] | year | (optional) defaults to undefined|


### Return type

**AssetReportListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자산 리포트 월 목록 조회 성공 |  -  |
|**400** | 잘못된 year, cursor 또는 size |  -  |
|**401** | Access Token 누락·만료·위조 |  -  |
|**403** | 해당 자녀의 부모 권한 없음 |  -  |
|**404** | 활성 자녀 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getBalanceHistoryUsingGET**
> AccountBalanceHistoryResponse getBalanceHistoryUsingGET()

최근 N개월(기본 6개월, 최대 12개월)의 월별 마지막 잔액과 직전 달 대비 순변화액을 조회합니다. 월 경계는 Asia/Seoul 기준이며, 응답 시각은 UTC입니다. change_amount는 실제 저축액이 아니라 월말 잔액의 순변화액입니다. 스냅샷이 없는 월은 null로 반환하며 이전 잔액을 이월하지 않습니다. 이 API는 저장된 잔액 스냅샷만 조회하고 CODEF API를 직접 호출하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let months: number; //현재 월을 포함한 조회 개월 수(1~12) (optional) (default to 6)

const { status, data } = await apiInstance.getBalanceHistoryUsingGET(
    accountId,
    authorization,
    months
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **months** | [**number**] | 현재 월을 포함한 조회 개월 수(1~12) | (optional) defaults to 6|


### Return type

**AccountBalanceHistoryResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 계좌 월별 잔액 변화 조회 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID 또는 조회 개월 수 |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 금융 계좌에 접근할 권한이 없음 |  -  |
|**404** | 계좌가 없거나 유효한 금융 연결 대상이 아님 |  -  |
|**500** | 저장된 잔액 스냅샷이 올바르지 않음 또는 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getBookmarksUsingGET**
> FinancialProductBookmarkListResponse getBookmarksUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let page: number; //page (optional) (default to undefined)
let productType: string; //product_type (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)

const { status, data } = await apiInstance.getBookmarksUsingGET(
    childId,
    authorization,
    page,
    productType,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **page** | [**number**] | page | (optional) defaults to undefined|
| **productType** | [**string**] | product_type | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|


### Return type

**FinancialProductBookmarkListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChecklistItemsUsingGET**
> ChecklistItemListResponse getChecklistItemsUsingGET()

연결된 보호자가 자녀의 생애주기별 체크리스트와 진행률을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (default to undefined)
let childId: number; //자녀 프로필 ID (default to undefined)
let stage: string; //PREGNANCY, AGE_0_TO_1, AGE_2_TO_4, AGE_5_TO_7, AGE_8_TO_10, AGE_11_TO_13, AGE_14_TO_16, AGE_17_TO_19 (optional) (default to undefined)

const { status, data } = await apiInstance.getChecklistItemsUsingGET(
    authorization,
    childId,
    stage
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | defaults to undefined|
| **childId** | [**number**] | 자녀 프로필 ID | defaults to undefined|
| **stage** | [**string**] | PREGNANCY, AGE_0_TO_1, AGE_2_TO_4, AGE_5_TO_7, AGE_8_TO_10, AGE_11_TO_13, AGE_14_TO_16, AGE_17_TO_19 | (optional) defaults to undefined|


### Return type

**ChecklistItemListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChildAccountsUsingGET**
> ChildAccountListResponse getChildAccountsUsingGET()

해당 자녀와 연결된 부모 또는 자녀 본인이 자녀 명의의 활성 Mock 계좌 목록을 조회합니다. 자녀 계좌 카드에 표시할 잔액 합계와 연결 계좌 수를 함께 반환합니다. 계좌별로 상세 이동과 화면 표시에 필요한 계좌 ID, 계좌명, 전체 계좌번호, 상품 유형, 현재 잔액만 반환합니다. 연결 계좌가 없으면 잔액 합계와 계좌 수가 0인 빈 목록을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getChildAccountsUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildAccountListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 계좌 목록 조회 성공 |  -  |
|**400** | 올바르지 않은 자녀 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 자녀에 접근할 권한 없음 |  -  |
|**404** | 자녀가 없거나 삭제 상태 |  -  |
|**500** | 계좌번호 복호화 실패 또는 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChildMemberLinkUsingGET**
> ChildMemberLinkResponse getChildMemberLinkUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getChildMemberLinkUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildMemberLinkResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChildTransfersUsingGET**
> TransferListResponseChildTransferListItemResponse getChildTransfersUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let endDate: string; //end_date (optional) (default to undefined)
let financialGoalId: number; //financial_goal_id (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)
let startDate: string; //start_date (optional) (default to undefined)
let status: string; //status (optional) (default to undefined)

const { status, data } = await apiInstance.getChildTransfersUsingGET(
    childId,
    authorization,
    cursor,
    endDate,
    financialGoalId,
    size,
    startDate,
    status
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **endDate** | [**string**] | end_date | (optional) defaults to undefined|
| **financialGoalId** | [**number**] | financial_goal_id | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|
| **startDate** | [**string**] | start_date | (optional) defaults to undefined|
| **status** | [**string**] | status | (optional) defaults to undefined|


### Return type

**TransferListResponseChildTransferListItemResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChildUsingGET**
> ChildResponse getChildUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //자녀 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getChildUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | 자녀 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getChildrenUsingGET**
> ChildListResponse getChildrenUsingGET()

현재 회원이 관리하는 자녀 목록을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getChildrenUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCurrentMonthUsageUsingGET**
> ChildAvailableAmountResponse getCurrentMonthUsageUsingGET()

자녀 회원이 본인의 대표 입출금 계좌에 설정된 월간 사용 관리 기준액과 현재 달 출금 합계, 남은 참고 금액을 조회합니다. 이 응답은 실제 금융기관의 결제·출금 가능 금액이나 거래 차단을 의미하지 않습니다. UNRESTRICTED 정책에서는 기준액·남은 금액·초과 여부를 null로 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getCurrentMonthUsageUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildAvailableAmountResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 본인 월간 사용 현황 조회 성공 |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 자녀 회원 계정이 아님 |  -  |
|**404** | 활성 자녀 프로필 또는 대표 입출금 계좌를 찾을 수 없음 |  -  |
|**409** | 대표 계좌의 사용 관리 정책이 설정되지 않음 |  -  |
|**500** | 저장된 사용 관리 데이터가 올바르지 않음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getDashboardUsingGET**
> ChildDashboardResponse getDashboardUsingGET()

자녀 본인의 월간 사용 현황, 용돈 요청, 최근 거래, 미션 및 읽지 않은 알림 수를 조회합니다. 대표 입출금 계좌가 없으면 spending_summary는 null입니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getDashboardUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildDashboardResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 대시보드 조회 성공 |  -  |
|**401** | Access Token 누락·만료·위조 |  -  |
|**403** | 자녀 회원 계정이 아님 |  -  |
|**404** | 로그인 회원과 연결된 활성 자녀 프로필 없음 |  -  |
|**409** | 대표 계좌의 자녀 사용 관리 정책이 설정되지 않음 |  -  |
|**500** | 저장된 대시보드 집계 데이터가 올바르지 않음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getDashboardUsingGET1**
> ParentDashboardResponse getDashboardUsingGET1()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (default to undefined)
let childId: number; //childId (default to undefined)

const { status, data } = await apiInstance.getDashboardUsingGET1(
    authorization,
    childId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | defaults to undefined|
| **childId** | [**number**] | childId | defaults to undefined|


### Return type

**ParentDashboardResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getDiscoveredAccountsUsingGET**
> DiscoveredAccountListResponse getDiscoveredAccountsUsingGET()

부모가 본인 또는 연결 자녀의 아직 연결하지 않은 Mock 계좌 후보를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let ownerType: string; //owner_type (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let childId: number; //child_id (optional) (default to undefined)

const { status, data } = await apiInstance.getDiscoveredAccountsUsingGET(
    ownerType,
    authorization,
    childId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **ownerType** | [**string**] | owner_type | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **childId** | [**number**] | child_id | (optional) defaults to undefined|


### Return type

**DiscoveredAccountListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 조회 성공. 후보가 없으면 빈 배열 |  -  |
|**400** | 조회 조건 오류 |  -  |
|**401** | Access Token 오류 |  -  |
|**403** | 부모 또는 자녀 접근 권한 없음 |  -  |
|**404** | 자녀 없음 |  -  |
|**409** | 부모 입출금계좌 온보딩 필요 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFamilyInvitationInfoUsingGET**
> FamilyInvitationInfoResponse getFamilyInvitationInfoUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let inviteToken: string; //초대 토큰 (default to undefined)

const { status, data } = await apiInstance.getFamilyInvitationInfoUsingGET(
    inviteToken
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **inviteToken** | [**string**] | 초대 토큰 | defaults to undefined|


### Return type

**FamilyInvitationInfoResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getFamilyMembersUsingGET**
> FamilyGuardianListResponse getFamilyMembersUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getFamilyMembersUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FamilyGuardianListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getGoalUsingGET**
> FinancialGoalDetailResponse getGoalUsingGET()

부모가 접근 가능한 자녀의 단일 금융 목표와 연결 적금 계좌, 현재 금액, 남은 금액, 달성률 및 체크포인트를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialGoalId: number; //financial_goal_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getGoalUsingGET(
    financialGoalId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **financialGoalId** | [**number**] | financial_goal_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 금융 목표 상세 조회 성공 |  -  |
|**400** | 금융 목표 ID 오류 |  -  |
|**401** | 유효하지 않은 Access Token |  -  |
|**403** | 부모 권한 없음 |  -  |
|**404** | 금융 목표를 찾을 수 없음 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getGoalsUsingGET**
> FinancialGoalListResponse getGoalsUsingGET()

부모가 접근 가능한 자녀의 진행 중·달성 목표와 연결 적금 계좌, 현재 금액, 남은 금액 및 달성률을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getGoalsUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 금융 목표 목록 조회 성공 |  -  |
|**400** | 자녀 ID 오류 |  -  |
|**401** | 유효하지 않은 Access Token |  -  |
|**403** | 부모 권한 또는 자녀 접근 권한 없음 |  -  |
|**404** | 자녀를 찾을 수 없음 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getLatestBalanceUsingGET**
> AccountBalanceResponse getLatestBalanceUsingGET()

마지막 금융정보 동기화로 저장된 계좌 잔액과 기준 시각을 조회합니다. 이 API에서는 CODEF 등 외부 금융기관 API를 직접 호출하지 않습니다. 부모 명의 계좌는 금융 연결 회원 본인, 자녀 명의 계좌는 해당 자녀와 연결된 부모 또는 자녀 본인만 조회할 수 있습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getLatestBalanceUsingGET(
    accountId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AccountBalanceResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 계좌 최신 잔액 조회 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 금융 계좌에 접근할 권한이 없음 |  -  |
|**404** | 계좌가 없거나 유효한 금융 연결 대상이 아님 |  -  |
|**409** | 아직 동기화된 계좌 잔액이 없음 |  -  |
|**500** | 서버 내부 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMemberTransfersUsingGET**
> TransferListResponseMemberTransferListItemResponse getMemberTransfersUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)
let childId: number; //child_id (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let endDate: string; //end_date (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)
let startDate: string; //start_date (optional) (default to undefined)
let status: string; //status (optional) (default to undefined)
let transferType: string; //transfer_type (optional) (default to undefined)

const { status, data } = await apiInstance.getMemberTransfersUsingGET(
    authorization,
    childId,
    cursor,
    endDate,
    size,
    startDate,
    status,
    transferType
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **childId** | [**number**] | child_id | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **endDate** | [**string**] | end_date | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|
| **startDate** | [**string**] | start_date | (optional) defaults to undefined|
| **status** | [**string**] | status | (optional) defaults to undefined|
| **transferType** | [**string**] | transfer_type | (optional) defaults to undefined|


### Return type

**TransferListResponseMemberTransferListItemResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMissionDetailUsingGET**
> MissionDetailResponse getMissionDetailUsingGET()

연결된 부모 또는 해당 자녀 본인이 미션 상세 정보를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let missionId: number; //mission_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getMissionDetailUsingGET(
    missionId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **missionId** | [**number**] | mission_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**MissionDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 미션 상세 조회 성공 |  -  |
|**400** | 미션 ID 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 미션 접근 권한 없음 |  -  |
|**404** | 미션 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMissionsUsingGET**
> MissionListResponse getMissionsUsingGET()

연결된 부모와 해당 자녀 본인이 미션 목록을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let filter: string; //filter (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)

const { status, data } = await apiInstance.getMissionsUsingGET(
    childId,
    authorization,
    cursor,
    filter,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **filter** | [**string**] | filter | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|


### Return type

**MissionListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 미션 목록 조회 성공 |  -  |
|**400** | 조회 조건 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 자녀 접근 권한 없음 |  -  |
|**404** | 자녀 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMyAccountsUsingGET**
> ParentAccountListResponse getMyAccountsUsingGET()

로그인한 부모 본인 명의의 활성 Mock 계좌를 조회합니다. 부모 계좌 카드에 표시할 잔액 합계와 연결 계좌 수를 함께 반환합니다. 계좌별로 상세 이동과 화면 표시에 필요한 계좌 ID, 계좌명, 전체 계좌번호, 상품 유형, 현재 잔액만 반환합니다. 연결 계좌가 없으면 잔액 합계와 계좌 수가 0인 빈 목록을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getMyAccountsUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ParentAccountListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 부모 계좌 목록 조회 성공 |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 부모 회원 권한 없음 |  -  |
|**500** | 계좌번호 복호화 실패 또는 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getMyProfileUsingGET**
> MemberProfileResponse getMyProfileUsingGET()

Access Token을 기준으로 현재 로그인 회원의 기본 정보와 연결된 소셜 계정 목록을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getMyProfileUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**MemberProfileResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 내 회원 정보 조회 성공 |  -  |
|**401** | Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getNotificationPreferencesUsingGET**
> NotificationPreferenceListResponse getNotificationPreferencesUsingGET()

현재 로그인한 회원의 알림 카테고리별 수신 설정을 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getNotificationPreferencesUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**NotificationPreferenceListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getNotificationsUsingGET**
> NotificationListResponse getNotificationsUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let afterId: string; //after_id (optional) (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let category: string; //category (optional) (default to undefined)
let childId: number; //child_id (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let isRead: string; //is_read (optional) (default to undefined)
let notificationType: string; //notification_type (optional) (default to undefined)
let size: string; //size (optional) (default to undefined)

const { status, data } = await apiInstance.getNotificationsUsingGET(
    afterId,
    authorization,
    category,
    childId,
    cursor,
    isRead,
    notificationType,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **afterId** | [**string**] | after_id | (optional) defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **category** | [**string**] | category | (optional) defaults to undefined|
| **childId** | [**number**] | child_id | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **isRead** | [**string**] | is_read | (optional) defaults to undefined|
| **notificationType** | [**string**] | notification_type | (optional) defaults to undefined|
| **size** | [**string**] | size | (optional) defaults to undefined|


### Return type

**NotificationListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getPermissionUsingGET**
> ChildFeaturePermissionResponse getPermissionUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getPermissionUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildFeaturePermissionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 이용 권한 조회 성공 |  -  |
|**401** | Access Token이 없거나 유효하지 않음 |  -  |
|**403** | 해당 자녀 정보에 접근할 부모 권한이 없음 |  -  |
|**404** | 자녀 정보를 찾을 수 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getPregnancyStatusUsingGET**
> PregnancyStatusResponse getPregnancyStatusUsingGET()

출산 예정일과 현재 날짜를 기준으로 임신 주차와 캐릭터를 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getPregnancyStatusUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**PregnancyStatusResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProductDetailUsingGET**
> FinancialProductDetailResponse getProductDetailUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialProductId: number; //financial_product_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let childId: number; //child_id (optional) (default to undefined)

const { status, data } = await apiInstance.getProductDetailUsingGET(
    financialProductId,
    authorization,
    childId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **financialProductId** | [**number**] | financial_product_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **childId** | [**number**] | child_id | (optional) defaults to undefined|


### Return type

**FinancialProductDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getProductsUsingGET**
> FinancialProductListResponse getProductsUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let productType: string; //product_type (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)

const { status, data } = await apiInstance.getProductsUsingGET(
    authorization,
    cursor,
    productType,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **productType** | [**string**] | product_type | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|


### Return type

**FinancialProductListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getReportUsingGET**
> ChildcareReportDetailResponse getReportUsingGET()

자녀 입출금계좌의 외부 출금 거래를 기준으로 최근 12개월 월별 지출과 연간 합계를 조회합니다. 자녀 계좌 사이의 내부 이체는 제외합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let month: number; //month (default to undefined)
let year: number; //year (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getReportUsingGET(
    childId,
    month,
    year,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **month** | [**number**] | month | defaults to undefined|
| **year** | [**number**] | year | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildcareReportDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 월간 양육비 리포트 조회 성공 |  -  |
|**400** | 잘못된 자녀 ID, 연도 또는 월 |  -  |
|**401** | Access Token 누락·만료·위조 |  -  |
|**403** | 해당 자녀의 부모 권한 없음 |  -  |
|**404** | 활성 자녀 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getScheduleDetailUsingGET**
> AutoTransferScheduleDetailResponse getScheduleDetailUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let scheduleId: number; //schedule_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getScheduleDetailUsingGET(
    scheduleId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **scheduleId** | [**number**] | schedule_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AutoTransferScheduleDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자동이체 일정 상세 조회 성공 |  -  |
|**400** | 일정 ID 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 자녀 접근 권한 없음 |  -  |
|**404** | 자동이체 일정 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSchedulesUsingGET**
> AutoTransferScheduleListResponse getSchedulesUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //cursor (optional) (default to undefined)
let size: number; //size (optional) (default to undefined)
let status: string; //status (optional) (default to undefined)

const { status, data } = await apiInstance.getSchedulesUsingGET(
    childId,
    authorization,
    cursor,
    size,
    status
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | cursor | (optional) defaults to undefined|
| **size** | [**number**] | size | (optional) defaults to undefined|
| **status** | [**string**] | status | (optional) defaults to undefined|


### Return type

**AutoTransferScheduleListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자동이체 일정 목록 조회 성공 |  -  |
|**400** | 조회 조건 오류 |  -  |
|**403** | 자녀 접근 권한 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTemplatesUsingGET**
> FinancialGoalTemplateListResponse getTemplatesUsingGET()

적금 목표 생성 화면에서 사용할 서비스 기본 목표 템플릿을 표시 순서대로 조회합니다. 직접 입력 목표는 포함하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTemplatesUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalTemplateListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 금융 목표 템플릿 조회 성공 |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTimeCapsuleEntriesUsingGET**
> TimeCapsuleEntryListResponse getTimeCapsuleEntriesUsingGET()

부모가 보관함 요약과 봉인된 엔트리 목록을 리스트·캘린더 공용 응답으로 조회합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let timeCapsuleId: number; //타임캡슐 보관함 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTimeCapsuleEntriesUsingGET(
    timeCapsuleId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **timeCapsuleId** | [**number**] | 타임캡슐 보관함 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TimeCapsuleEntryListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTimeCapsuleEntryUsingGET**
> TimeCapsuleEntryDetailResponse getTimeCapsuleEntryUsingGET()

공개일이 도래한 타임캡슐의 봉인된 엔트리를 연결된 보호자 또는 자녀 본인이 조회합니다. 편지, 기여 정보, 엔트리 순번과 화면 표시용 단일 이미지 임시 조회 URL을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let entryId: number; //타임캡슐 엔트리 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTimeCapsuleEntryUsingGET(
    entryId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **entryId** | [**number**] | 타임캡슐 엔트리 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TimeCapsuleEntryDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTimeCapsulesUsingGET**
> TimeCapsuleListResponse getTimeCapsulesUsingGET()

부모가 접근 가능한 자녀의 타임캡슐 보관함을 공개 날짜 순서로 조회합니다. 카드 화면에 필요한 공개일·D-day·총 저축 금액을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //자녀 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTimeCapsulesUsingGET(
    childId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **childId** | [**number**] | 자녀 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TimeCapsuleListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTransactionDetailUsingGET**
> AccountTransactionDetailResponse getTransactionDetailUsingGET()

거래 ID로 거래 금액, 메모, 입금처, 출금처, 거래 시각, 거래 후 잔액을 조회합니다. 거래가 속한 계좌를 기준으로 부모 본인 또는 연결 부모·자녀 회원의 접근 권한을 검증합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountTransactionId: number; //account_transaction_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTransactionDetailUsingGET(
    accountTransactionId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountTransactionId** | [**number**] | account_transaction_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AccountTransactionDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 거래내역 상세 조회 성공 |  -  |
|**400** | 올바르지 않은 거래 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 거래 원장 계좌 접근 권한 없음 |  -  |
|**404** | 거래내역을 찾을 수 없음 |  -  |
|**500** | 계좌번호 복호화 실패, 저장 데이터 불일치 또는 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTransactionsUsingGET**
> AccountTransactionListResponse getTransactionsUsingGET()

계좌별 거래내역을 거래 발생 시각과 거래 ID 내림차순으로 커서 페이지네이션 조회합니다. 목록 화면에 필요한 거래 ID, 거래 시각, 거래 상대 표시명, 입출금 방향, 금액만 반환합니다. 거래가 없으면 빈 목록을 반환합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let cursor: string; //이전 응답의 next_cursor (optional) (default to undefined)
let size: number; //페이지 조회 개수(1~100, 기본 20) (optional) (default to undefined)

const { status, data } = await apiInstance.getTransactionsUsingGET(
    accountId,
    authorization,
    cursor,
    size
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **cursor** | [**string**] | 이전 응답의 next_cursor | (optional) defaults to undefined|
| **size** | [**number**] | 페이지 조회 개수(1~100, 기본 20) | (optional) defaults to undefined|


### Return type

**AccountTransactionListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 계좌 거래내역 목록 조회 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID, 커서 또는 조회 개수 |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 금융 계좌 접근 권한 없음 |  -  |
|**404** | 계좌가 없거나 활성 연결 계좌가 아님 |  -  |
|**500** | 저장 거래 데이터 불일치 또는 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTransferUsingGET**
> TransferDetailResponse getTransferUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let transferId: number; //transfer_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getTransferUsingGET(
    transferId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **transferId** | [**number**] | transfer_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TransferDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getUnreadCountUsingGET**
> NotificationUnreadCountResponse getUnreadCountUsingGET()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getUnreadCountUsingGET(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**NotificationUnreadCountResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getUsagePolicyUsingGET**
> ChildAccountUsagePolicyResponse getUsagePolicyUsingGET()

해당 자녀의 부모 또는 계좌에 연결된 자녀 회원이 현재 사용 관리 정책을 조회합니다. 정책은 실제 금융기관 거래를 차단하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.getUsagePolicyUsingGET(
    accountId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildAccountUsagePolicyResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 계좌 사용 관리 정책 조회 성공 |  -  |
|**401** | Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원 |  -  |
|**403** | 해당 자녀 정보에 접근할 권한이 없음 |  -  |
|**404** | 금융 계좌를 찾을 수 없음 |  -  |
|**422** | 자녀 명의의 활성 입출금 계좌가 아님 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **linkUsingPOST**
> AccountLinkResponse linkUsingPOST()


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    AccountLinkRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)
let request: AccountLinkRequest; //request (optional)

const { status, data } = await apiInstance.linkUsingPOST(
    authorization,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **AccountLinkRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AccountLinkResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **loginUsingPOST**
> OAuthLoginResponse loginUsingPOST(request)

Google 또는 Kakao의 인가 코드로 로그인하며, 최초 로그인 시 회원을 생성합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    OAuthLoginRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let provider: 'google' | 'kakao'; //소셜 로그인 제공자 (default to undefined)
let request: OAuthLoginRequest; //request

const { status, data } = await apiInstance.loginUsingPOST(
    provider,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **OAuthLoginRequest**| request | |
| **provider** | [**&#39;google&#39; | &#39;kakao&#39;**]**Array<&#39;google&#39; &#124; &#39;kakao&#39;>** | 소셜 로그인 제공자 | defaults to undefined|


### Return type

**OAuthLoginResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 로그인 또는 회원가입 성공 |  -  |
|**400** | 요청값 오류 또는 지원하지 않는 제공자 |  -  |
|**401** | 만료되었거나 유효하지 않은 인가 코드 |  -  |
|**502** | 소셜 제공자 통신 실패 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **loginWithChildInviteUsingPOST**
> ChildInviteOAuthResponse loginWithChildInviteUsingPOST(request)

자녀 초대 토큰과 소셜 인가 코드를 검증하고 CHILD 회원을 생성하거나 로그인합니다. 이 단계에서는 자녀 정보와 회원 계정을 연결하지 않으며, 가족 초대 상태를 PENDING으로 유지합니다. 로그인 성공 후 별도의 가족 초대 수락 API를 호출해야 합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChildInviteOAuthRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let provider: 'google' | 'kakao'; //소셜 로그인 제공자 (default to undefined)
let request: ChildInviteOAuthRequest; //request

const { status, data } = await apiInstance.loginWithChildInviteUsingPOST(
    provider,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChildInviteOAuthRequest**| request | |
| **provider** | [**&#39;google&#39; | &#39;kakao&#39;**]**Array<&#39;google&#39; &#124; &#39;kakao&#39;>** | 소셜 로그인 제공자 | defaults to undefined|


### Return type

**ChildInviteOAuthResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 로그인 및 수락 대기 초대 정보 조회 성공 |  -  |
|**400** | 요청값 오류, 지원하지 않는 제공자 또는 사용할 수 없는 자녀 초대 |  -  |
|**401** | 유효하지 않은 인가 코드 또는 탈퇴 회원 |  -  |
|**409** | 초대 유형과 기존 회원 유형 불일치 |  -  |
|**502** | 소셜 제공자 통신 실패 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **loginWithParentInviteUsingPOST**
> ParentInviteOAuthResponse loginWithParentInviteUsingPOST(request)

부모 초대 토큰과 소셜 인가 코드를 검증하고 PARENT 회원을 생성하거나 로그인합니다. 이 단계에서는 부모·자녀 관계를 등록하지 않으며, 가족 초대 상태를 PENDING으로 유지합니다. 부모 관계 유형은 별도의 가족 초대 수락 API에서 전달합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ParentInviteOAuthRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let provider: 'google' | 'kakao'; //소셜 로그인 제공자 (default to undefined)
let request: ParentInviteOAuthRequest; //request

const { status, data } = await apiInstance.loginWithParentInviteUsingPOST(
    provider,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ParentInviteOAuthRequest**| request | |
| **provider** | [**&#39;google&#39; | &#39;kakao&#39;**]**Array<&#39;google&#39; &#124; &#39;kakao&#39;>** | 소셜 로그인 제공자 | defaults to undefined|


### Return type

**ParentInviteOAuthResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 로그인 및 수락 대기 초대 정보 조회 성공 |  -  |
|**400** | 요청값 오류, 지원하지 않는 제공자 또는 사용할 수 없는 부모 초대 |  -  |
|**401** | 유효하지 않은 인가 코드 또는 탈퇴 회원 |  -  |
|**409** | 초대 유형과 기존 회원 유형 불일치 |  -  |
|**502** | 소셜 제공자 통신 실패 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **logoutUsingPOST**
> logoutUsingPOST(request)

현재 기기에서 사용 중인 Refresh Token을 폐기합니다. 이미 폐기됐거나 존재하지 않는 토큰 요청도 성공으로 처리합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    TokenLogoutRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: TokenLogoutRequest; //request

const { status, data } = await apiInstance.logoutUsingPOST(
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **TokenLogoutRequest**| request | |


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 로그아웃 성공 |  -  |
|**400** | Refresh Token 누락 또는 빈 값 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **openUsingPOST**
> AccountOpenResponse openUsingPOST()

실제 금융기관 호출 없이 KB 금융상품 정보로 Mock 계좌를 개설합니다. 자녀 적금 목표는 계좌 개설 후 GOAL-2에서 별도로 생성하고 연결합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    AccountOpenRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)
let request: AccountOpenRequest; //request (optional)

const { status, data } = await apiInstance.openUsingPOST(
    authorization,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **AccountOpenRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AccountOpenResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **readAllNotificationsUsingPATCH**
> NotificationReadAllResponse readAllNotificationsUsingPATCH()

현재 로그인한 회원의 모든 읽지 않은 알림을 읽음 처리합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.readAllNotificationsUsingPATCH(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**NotificationReadAllResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **readNotificationUsingPATCH**
> NotificationReadResponse readNotificationUsingPATCH()

현재 로그인한 회원의 알림 한 건을 읽음 처리합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let notificationId: number; //notification_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.readNotificationUsingPATCH(
    notificationId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **notificationId** | [**number**] | notification_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**NotificationReadResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **refreshUsingPOST**
> TokenRefreshResponse refreshUsingPOST(request)

유효한 Refresh Token을 새 Access Token과 Refresh Token으로 교체합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    TokenRefreshRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: TokenRefreshRequest; //request

const { status, data } = await apiInstance.refreshUsingPOST(
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **TokenRefreshRequest**| request | |


### Return type

**TokenRefreshResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 토큰 재발급 성공 |  -  |
|**400** | Refresh Token 누락 또는 빈 값 |  -  |
|**401** | 유효하지 않거나 만료·폐기된 Refresh Token |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **registerUsingPOST**
> PushDeviceResponse registerUsingPOST(request)

동일한 device_key가 이미 있으면 FCM 토큰과 기기 정보를 갱신합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RegisterPushDeviceRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: RegisterPushDeviceRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.registerUsingPOST(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **RegisterPushDeviceRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**PushDeviceResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **retryUsingPOST**
> AutoTransferRetryResponse retryUsingPOST()

가장 최근 실패한 자동이체 회차를 다시 실행합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let scheduleId: number; //schedule_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)
let idempotencyKey: string; //Idempotency-Key (optional) (default to undefined)

const { status, data } = await apiInstance.retryUsingPOST(
    scheduleId,
    authorization,
    idempotencyKey
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **scheduleId** | [**number**] | schedule_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|
| **idempotencyKey** | [**string**] | Idempotency-Key | (optional) defaults to undefined|


### Return type

**AutoTransferRetryResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 재시도 처리 완료 |  -  |
|**400** | 요청 형식 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 재시도 권한 없음 |  -  |
|**404** | 일정 없음 |  -  |
|**409** | 재시도 불가 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **sealTimeCapsuleEntryUsingPATCH**
> TimeCapsuleEntrySealResponse sealTimeCapsuleEntryUsingPATCH()

작성자 본인의 DRAFT 엔트리에 단일 활성 이미지가 등록된 경우 엔트리를 최종 봉인합니다. 봉인된 엔트리는 보관함 목록·캘린더에 노출되며 이후 내용과 미디어를 수정할 수 없습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let entryId: number; //타임캡슐 엔트리 ID (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.sealTimeCapsuleEntryUsingPATCH(
    entryId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **entryId** | [**number**] | 타임캡슐 엔트리 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**TimeCapsuleEntrySealResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **sendVerificationCodeUsingPOST**
> PhoneVerificationSendResponse sendVerificationCodeUsingPOST(request)

현재 로그인 회원의 휴대폰으로 6자리 인증번호를 발송합니다. 인증번호는 3분 동안 유효하며 재발송은 60초 후 가능합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    PhoneVerificationSendRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: PhoneVerificationSendRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.sendVerificationCodeUsingPOST(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **PhoneVerificationSendRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**PhoneVerificationSendResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**202** | 인증번호 발송 요청 성공 |  -  |
|**400** | 휴대폰 번호 누락 또는 형식 오류 |  -  |
|**401** | Access Token 누락·만료 또는 유효하지 않음 |  -  |
|**429** | 인증번호 재발송 대기시간이 지나지 않음 |  -  |
|**502** | SMS 제공자 발송 실패 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **setPrimaryAccountUsingPATCH**
> setPrimaryAccountUsingPATCH()

로그인 회원이 접근할 수 있는 활성 계좌를 대표 계좌로 설정합니다. 부모 명의 계좌는 금융 연결을 생성한 부모 본인만, 자녀 명의 계좌는 해당 자녀와 연결된 부모 또는 자녀 본인이 설정할 수 있습니다. 같은 소유 범위의 기존 대표 계좌는 자동으로 해제되며, 이미 대표인 계좌에 다시 요청해도 성공으로 처리합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.setPrimaryAccountUsingPATCH(
    accountId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 대표 계좌 설정 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 금융 계좌에 접근할 권한이 없음 |  -  |
|**404** | 계좌가 없거나 대표 계좌로 설정할 수 없는 상태 |  -  |
|**500** | 서버 내부 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **unlinkAccountUsingDELETE**
> unlinkAccountUsingDELETE()

금융기관 계좌를 해지하거나 CODEF 금융 연결 전체를 철회하지 않고 선택한 계좌의 Azas 서비스 연결만 해제합니다. 해당 금융 연결을 생성한 회원만 요청할 수 있으며, 이미 연결 해제된 계좌에 다시 요청해도 성공으로 처리합니다. 계좌·거래·잔액 이력과 기존 관리 설정은 삭제하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.unlinkAccountUsingDELETE(
    accountId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 계좌 서비스 연결 해제 성공 |  -  |
|**400** | 올바르지 않은 계좌 ID |  -  |
|**401** | Access Token 누락·만료·위조 또는 탈퇴 회원 |  -  |
|**403** | 해당 금융 연결을 생성한 회원이 아님 |  -  |
|**404** | 금융 계좌를 찾을 수 없음 |  -  |
|**500** | 서버 내부 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **unregisterUsingDELETE**
> unregisterUsingDELETE()

로그아웃하거나 푸시 권한을 해제한 기기를 비활성화합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let pushDeviceId: number; //push_device_id (default to undefined)
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.unregisterUsingDELETE(
    pushDeviceId,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **pushDeviceId** | [**number**] | push_device_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateAllowanceRequestStatusUsingPATCH**
> AllowanceRequestDetailResponse updateAllowanceRequestStatusUsingPATCH(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    UpdateAllowanceRequestStatus
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let allowanceRequestId: number; //allowance_request_id (default to undefined)
let request: UpdateAllowanceRequestStatus; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateAllowanceRequestStatusUsingPATCH(
    allowanceRequestId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **UpdateAllowanceRequestStatus**| request | |
| **allowanceRequestId** | [**number**] | allowance_request_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AllowanceRequestDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateBookmarkUsingPUT**
> FinancialProductBookmarkResponse updateBookmarkUsingPUT(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FinancialProductBookmarkRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let financialProductId: number; //financial_product_id (default to undefined)
let request: FinancialProductBookmarkRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateBookmarkUsingPUT(
    childId,
    financialProductId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FinancialProductBookmarkRequest**| request | |
| **childId** | [**number**] | child_id | defaults to undefined|
| **financialProductId** | [**number**] | financial_product_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialProductBookmarkResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateChecklistItemCompletionUsingPATCH**
> ChecklistItemCompletionResponse updateChecklistItemCompletionUsingPATCH(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChecklistItemCompletionRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (default to undefined)
let checklistItemId: number; //checklist_item_id (default to undefined)
let request: ChecklistItemCompletionRequest; //request

const { status, data } = await apiInstance.updateChecklistItemCompletionUsingPATCH(
    authorization,
    checklistItemId,
    request
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChecklistItemCompletionRequest**| request | |
| **authorization** | [**string**] | Authorization | defaults to undefined|
| **checklistItemId** | [**number**] | checklist_item_id | defaults to undefined|


### Return type

**ChecklistItemCompletionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateChildUsingPATCH**
> ChildResponse updateChildUsingPATCH(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChildUpdateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //childId (default to undefined)
let request: ChildUpdateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateChildUsingPATCH(
    childId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChildUpdateRequest**| request | |
| **childId** | [**number**] | childId | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateGoalUsingPATCH**
> FinancialGoalDetailResponse updateGoalUsingPATCH(request)

부모가 목표 금액, 목표 달성일, 최종 연결 적금 계좌 목록을 부분 수정합니다. 목표 종류와 제목은 변경할 수 없으며 연결 적금은 한 개 이상 유지해야 합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    FinancialGoalUpdateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let financialGoalId: number; //financial_goal_id (default to undefined)
let request: FinancialGoalUpdateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateGoalUsingPATCH(
    financialGoalId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **FinancialGoalUpdateRequest**| request | |
| **financialGoalId** | [**number**] | financial_goal_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**FinancialGoalDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 금융 목표 수정 성공 |  -  |
|**400** | 금융 목표 수정 요청 오류 |  -  |
|**401** | 유효하지 않은 Access Token |  -  |
|**403** | 부모 권한 없음 |  -  |
|**404** | 목표 또는 적금 계좌를 찾을 수 없음 |  -  |
|**409** | 적금 계좌가 다른 목표에 이미 연결됨 |  -  |
|**422** | 목표에 연결할 수 없는 계좌 또는 이미 달성한 금액 |  -  |
|**500** | 서버 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateMissionStatusUsingPATCH**
> MissionDetailResponse updateMissionStatusUsingPATCH(request)

자녀 완료 요청 및 부모의 승인·거절·취소를 처리합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    UpdateMissionStatusRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let missionId: number; //mission_id (default to undefined)
let request: UpdateMissionStatusRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateMissionStatusUsingPATCH(
    missionId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **UpdateMissionStatusRequest**| request | |
| **missionId** | [**number**] | mission_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**MissionDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 미션 상태 변경 성공 |  -  |
|**400** | 상태 변경 요청 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 미션 처리 권한 없음 |  -  |
|**404** | 미션 또는 계좌 없음 |  -  |
|**409** | 상태 전이 또는 이체 오류 |  -  |
|**422** | 보상 계좌 조합 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateMyProfileUsingPATCH**
> MemberProfileResponse updateMyProfileUsingPATCH(request)

생년월일, 프로필 이미지 URL, 인증 완료된 휴대폰 번호를 수정합니다. 휴대폰 번호는 인증번호 확인 API에서 발급받은 토큰으로 변경합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    MemberProfileUpdateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: MemberProfileUpdateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateMyProfileUsingPATCH(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **MemberProfileUpdateRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**MemberProfileResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 내 회원 정보 수정 성공 |  -  |
|**400** | 수정 항목 또는 휴대폰 인증 토큰이 올바르지 않음 |  -  |
|**401** | Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원 |  -  |
|**409** | 이미 다른 회원이 사용 중인 휴대폰 번호 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateNotificationPreferencesUsingPUT**
> NotificationPreferenceListResponse updateNotificationPreferencesUsingPUT(request)

현재 로그인한 회원의 알림 카테고리별 수신 여부를 일괄 저장합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    UpdateNotificationPreferencesRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let request: UpdateNotificationPreferencesRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateNotificationPreferencesUsingPUT(
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **UpdateNotificationPreferencesRequest**| request | |
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**NotificationPreferenceListResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updatePermissionUsingPATCH**
> ChildFeaturePermissionResponse updatePermissionUsingPATCH(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChildFeaturePermissionRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let childId: number; //child_id (default to undefined)
let request: ChildFeaturePermissionRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updatePermissionUsingPATCH(
    childId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChildFeaturePermissionRequest**| request | |
| **childId** | [**number**] | child_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildFeaturePermissionResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 이용 권한 수정 성공 |  -  |
|**400** | 필수 권한 값이 누락되었거나 요청 값이 올바르지 않음 |  -  |
|**401** | Access Token이 없거나 유효하지 않음 |  -  |
|**403** | 해당 자녀 정보에 접근할 부모 권한이 없음 |  -  |
|**404** | 자녀 정보를 찾을 수 없음 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateScheduleUsingPATCH**
> AutoTransferScheduleDetailResponse updateScheduleUsingPATCH(request)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    UpdateAutoTransferScheduleRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let scheduleId: number; //schedule_id (default to undefined)
let request: UpdateAutoTransferScheduleRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateScheduleUsingPATCH(
    scheduleId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **UpdateAutoTransferScheduleRequest**| request | |
| **scheduleId** | [**number**] | schedule_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**AutoTransferScheduleDetailResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자동이체 일정 변경 성공 |  -  |
|**400** | 요청 형식 오류 |  -  |
|**401** | 인증 오류 |  -  |
|**403** | 일정 변경 권한 없음 |  -  |
|**404** | 자동이체 일정 없음 |  -  |
|**409** | 중복 일정 또는 상태 전이 오류 |  -  |
|**422** | 일정 조건 오류 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateTimeCapsuleReleaseDateUsingPATCH**
> UpdateTimeCapsuleReleaseDateResponse updateTimeCapsuleReleaseDateUsingPATCH(request)

부모는 연결된 입출금계좌 타임캡슐의 공개일을 오늘 이후 날짜로 설정하거나 변경할 수 있습니다. 적금 타임캡슐은 계좌 만기일을 공개일로 사용하므로 변경할 수 없습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    UpdateTimeCapsuleReleaseDateRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let timeCapsuleId: number; //타임캡슐 보관함 ID (default to undefined)
let request: UpdateTimeCapsuleReleaseDateRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateTimeCapsuleReleaseDateUsingPATCH(
    timeCapsuleId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **UpdateTimeCapsuleReleaseDateRequest**| request | |
| **timeCapsuleId** | [**number**] | 타임캡슐 보관함 ID | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**UpdateTimeCapsuleReleaseDateResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateUsagePolicyUsingPATCH**
> ChildAccountUsagePolicyResponse updateUsagePolicyUsingPATCH(request)

부모가 자녀 명의의 활성 입출금 계좌에 월간 사용 관리 기준을 설정합니다. 이 정책은 실제 금융기관의 결제나 이체를 차단하지 않습니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration,
    ChildAccountUsagePolicyRequest
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let accountId: number; //account_id (default to undefined)
let request: ChildAccountUsagePolicyRequest; //request
let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.updateUsagePolicyUsingPATCH(
    accountId,
    request,
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **request** | **ChildAccountUsagePolicyRequest**| request | |
| **accountId** | [**number**] | account_id | defaults to undefined|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

**ChildAccountUsagePolicyResponse**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 자녀 계좌 사용 관리 정책 설정 성공 |  -  |
|**400** | 사용 관리 모드 또는 관리 기준 금액이 올바르지 않음 |  -  |
|**401** | Access Token 누락·만료·유효하지 않음 또는 탈퇴 회원 |  -  |
|**403** | 해당 자녀에 대한 부모 권한이 없음 |  -  |
|**404** | 금융 계좌를 찾을 수 없음 |  -  |
|**422** | 자녀 명의의 활성 입출금 계좌가 아님 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **withdrawMyMembershipUsingDELETE**
> withdrawMyMembershipUsingDELETE()

현재 로그인 회원을 탈퇴 처리하고 해당 회원의 모든 활성 Refresh Token을 폐기합니다.

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let authorization: string; //Authorization (optional) (default to undefined)

const { status, data } = await apiInstance.withdrawMyMembershipUsingDELETE(
    authorization
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **authorization** | [**string**] | Authorization | (optional) defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |
|**204** | 회원 탈퇴 성공 |  -  |
|**401** | Access Token 누락·만료·유효하지 않음 또는 이미 탈퇴한 회원 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

