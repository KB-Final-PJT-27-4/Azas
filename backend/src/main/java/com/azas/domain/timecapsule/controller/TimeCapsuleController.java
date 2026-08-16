package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleExportRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlsRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlsResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryDetailResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportDownloadUrlResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleEntryService;
import com.azas.domain.timecapsule.service.TimeCapsuleExportService;
import com.azas.domain.timecapsule.service.TimeCapsuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "타임캡슐")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TimeCapsuleController {

    private final AccessTokenMemberResolver accessTokenMemberResolver;
    private final TimeCapsuleService timeCapsuleService;
    private final TimeCapsuleEntryService timeCapsuleEntryService;
    private final TimeCapsuleExportService timeCapsuleExportService;

    @ApiOperation(
            value = "TIMECAPSULE-1 타임캡슐 보관함 생성",
            notes = "부모가 접근 가능한 부모 또는 자녀의 활성 입출금·적금 계좌를 특정 자녀의 타임캡슐과 연결합니다. 보관함 제목은 계좌명으로 자동 생성되며 공개 날짜는 선택 입력입니다."
    )
    @PostMapping("/children/{child_id}/time-capsules")
    public ResponseEntity<CreateTimeCapsuleResponse> createTimeCapsule(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 대상 자녀 ID", required = true)
            @PathVariable("child_id")
            long childId,
            @Valid @RequestBody CreateTimeCapsuleRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timeCapsuleService.createTimeCapsule(
                        memberId,
                        childId,
                        request
                ));
    }

    @ApiOperation(
            value = "TIMECAPSULE-2 타임캡슐 보관함 목록 조회",
            notes = "부모가 접근 가능한 자녀의 타임캡슐 보관함을 공개 날짜 순서로 조회합니다. 카드 화면에 필요한 공개일·D-day·총 저축 금액을 반환합니다."
    )
    @GetMapping("/children/{child_id}/time-capsules")
    // [JMG] CAPSULE-2 부모에게 연결된 자녀의 타임캡슐 보관함 목록을 조회한다.
    public ResponseEntity<TimeCapsuleListResponse> getTimeCapsules(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "자녀 ID", required = true)
            @PathVariable("child_id")
            long childId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(timeCapsuleService.getTimeCapsules(
                memberId,
                childId
        ));
    }

    @ApiOperation(
            value = "TC-6 타임캡슐 보관함 삭제",
            notes = "부모 또는 보호자가 보관함과 내부 엔트리·미디어·결과물을 영구 삭제합니다."
    )
    @DeleteMapping("/time-capsules/{time_capsule_id}")
    // [JMG] CAPSULE-6 부모·보호자 권한을 확인한 뒤 타임캡슐 보관함을 영구 삭제한다.
    public ResponseEntity<Void> deleteTimeCapsule(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        timeCapsuleService.deleteTimeCapsule(memberId, timeCapsuleId);

        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "TC-11 타임캡슐 결과물 생성 요청",
            notes = "공개된 타임캡슐의 봉인 엔트리를 비동기 VIDEO 또는 ARCHIVE 결과물 작업으로 등록합니다."
    )
    @PostMapping("/time-capsules/{time_capsule_id}/exports")
    // [JMG] CAPSULE-11 공개 타임캡슐 결과물 생성 작업 등록 요청을 처리한다.
    public ResponseEntity<TimeCapsuleExportResponse>
    createTimeCapsuleExport(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId,
            @Valid @RequestBody CreateTimeCapsuleExportRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                timeCapsuleExportService.createTimeCapsuleExport(
                        memberId,
                        timeCapsuleId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "TC-9 타임캡슐 결과물 생성 상태 조회",
            notes = "부모 또는 보호자가 비동기 결과물 생성 작업의 상태와 결과 메타데이터를 조회합니다."
    )
    @GetMapping("/time-capsule-exports/{export_id}")
    // [JMG] CAPSULE-9 부모·보호자 권한을 확인한 뒤 결과물 생성 상태를 조회한다.
    public ResponseEntity<TimeCapsuleExportResponse>
    getTimeCapsuleExport(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 결과물 생성 ID", required = true)
            @PathVariable("export_id")
            long timeCapsuleExportId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(timeCapsuleExportService.getTimeCapsuleExport(
                memberId,
                timeCapsuleExportId
        ));
    }

    @ApiOperation(
            value = "TC-10 타임캡슐 결과물 다운로드 URL 발급",
            notes = "생성이 완료되고 보관 기간이 남은 결과물의 임시 다운로드 URL을 발급합니다."
    )
    @GetMapping("/time-capsule-exports/{export_id}/download-url")
    // [JMG] CAPSULE-10 완료·미만료 결과물의 임시 다운로드 URL을 발급한다.
    public ResponseEntity<TimeCapsuleExportDownloadUrlResponse>
    createTimeCapsuleExportDownloadUrl(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 결과물 생성 ID", required = true)
            @PathVariable("export_id")
            long timeCapsuleExportId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleExportService.createTimeCapsuleExportDownloadUrl(
                        memberId,
                        timeCapsuleExportId
                )
        );
    }

    @ApiOperation(
            value = "TIMECAPSULE-5 타임캡슐 기록 생성",
            notes = "부모가 타임캡슐 계좌의 입금 거래와 제목·편지를 선택해 DRAFT 기록을 생성합니다."
    )
    @PostMapping("/time-capsules/{time_capsule_id}/entries")
    public ResponseEntity<CreateTimeCapsuleEntryResponse>
    createTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId,
            @Valid @RequestBody CreateTimeCapsuleEntryRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                timeCapsuleEntryService.createTimeCapsuleEntry(
                        memberId,
                        timeCapsuleId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "TIMECAPSULE-4 타임캡슐 엔트리 목록 조회",
            notes = "부모가 보관함 요약과 봉인된 엔트리 목록을 리스트·캘린더 공용 응답으로 조회합니다."
    )
    @GetMapping("/time-capsules/{time_capsule_id}/entries")
    // [JMG] CAPSULE-4 부모 권한을 확인한 뒤 타임캡슐 내부 엔트리 목록을 조회한다.
    public ResponseEntity<TimeCapsuleEntryListResponse>
    getTimeCapsuleEntries(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 보관함 ID", required = true)
            @PathVariable("time_capsule_id")
            long timeCapsuleId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.getTimeCapsuleEntries(
                        memberId,
                        timeCapsuleId
                )
        );
    }

    @ApiOperation(
            value = "TC-14 타임캡슐 엔트리 상세 조회",
            notes = "부모 또는 보호자가 편지와 활성 미디어를 임시 다운로드 URL로 조회합니다."
    )
    @GetMapping("/time-capsule-entries/{entry_id}")
    // [JMG] CAPSULE-14 부모·보호자 권한을 확인한 뒤 타임캡슐 엔트리 상세를 조회한다.
    public ResponseEntity<TimeCapsuleEntryDetailResponse>
    getTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.getTimeCapsuleEntry(
                        memberId,
                        timeCapsuleEntryId
                )
        );
    }

    @ApiOperation(
            value = "TC-13 타임캡슐 엔트리 삭제",
            notes = "작성자 본인이 DRAFT 엔트리와 연결된 미디어를 삭제합니다."
    )
    @DeleteMapping("/time-capsule-entries/{entry_id}")
    // [JMG] CAPSULE-13 작성자 본인의 DRAFT 타임캡슐 엔트리 삭제 요청을 처리한다.
    public ResponseEntity<Void> deleteTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );
        timeCapsuleEntryService.deleteTimeCapsuleEntry(
                memberId,
                timeCapsuleEntryId
        );

        return ResponseEntity.noContent().build();
    }

    @ApiOperation(
            value = "TC-15 타임캡슐 엔트리 봉인",
            notes = "작성자 본인이 미디어 조건을 충족한 DRAFT 엔트리를 봉인합니다."
    )
    @PatchMapping("/time-capsule-entries/{entry_id}/seal")
    // [JMG] CAPSULE-15 미디어 조건을 충족한 작성자 본인의 DRAFT 엔트리를 봉인한다.
    public ResponseEntity<TimeCapsuleEntrySealResponse>
    sealTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.sealTimeCapsuleEntry(
                        memberId,
                        timeCapsuleEntryId
                )
        );
    }

    @ApiOperation(
            value = "TC-7 타임캡슐 엔트리 미디어 업로드 URL 발급",
            notes = "DRAFT 엔트리에 서버가 생성한 S3 Presigned PUT URL을 발급합니다."
    )
    @PostMapping("/time-capsule-entries/{entry_id}/media/upload-urls")
    // [JMG] CAPSULE-7 작성자 본인의 DRAFT 엔트리에 첨부할 미디어 업로드 URL 발급 요청을 처리한다.
    public ResponseEntity<CreateTimeCapsuleMediaUploadUrlsResponse>
    createMediaUploadUrls(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId,
            @Valid @RequestBody CreateTimeCapsuleMediaUploadUrlsRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                timeCapsuleEntryService.createMediaUploadUrls(
                        memberId,
                        timeCapsuleEntryId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "TC-8 타임캡슐 엔트리 미디어 업로드 완료",
            notes = "S3 객체 메타데이터를 검증한 뒤 업로드 대기 미디어를 활성화합니다."
    )
    @PostMapping("/time-capsule-entries/{entry_id}/media/complete")
    // [JMG] CAPSULE-8 S3 업로드가 끝난 작성자 본인의 미디어 완료 처리 요청을 검증한다.
    public ResponseEntity<CompleteTimeCapsuleMediaUploadResponse>
    completeMediaUpload(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId,
            @Valid @RequestBody CompleteTimeCapsuleMediaUploadRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.ok(
                timeCapsuleEntryService.completeMediaUpload(
                        memberId,
                        timeCapsuleEntryId,
                        request
                )
        );
    }
}
