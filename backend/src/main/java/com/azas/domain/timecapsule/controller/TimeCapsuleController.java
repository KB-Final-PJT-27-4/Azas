package com.azas.domain.timecapsule.controller;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleResponse;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadRequest;
import com.azas.domain.timecapsule.dto.CompleteTimeCapsuleMediaUploadResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleMediaUploadUrlResponse;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryRequest;
import com.azas.domain.timecapsule.dto.CreateTimeCapsuleEntryResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryListResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntryDetailResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleEntrySealResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleListResponse;
import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.domain.timecapsule.service.TimeCapsuleEntryService;
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
            value = "TIMECAPSULE-6 타임캡슐 보관함 삭제",
            notes = "접근 가능한 부모 또는 보호자가 보관함과 내부 엔트리·미디어 및 원본 저장 객체를 복구할 수 없도록 영구 삭제합니다. 자녀·계좌·거래·목표 데이터는 유지됩니다."
    )
    @DeleteMapping("/time-capsules/{time_capsule_id}")
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
            value = "TIMECAPSULE-14 공개된 타임캡슐 엔트리 상세 조회",
            notes = "공개일이 도래한 타임캡슐의 봉인된 엔트리를 연결된 보호자 또는 "
                    + "자녀 본인이 조회합니다. 편지, 기여 정보, 엔트리 순번과 "
                    + "화면 표시용 단일 이미지 임시 조회 URL을 반환합니다."
    )
    @GetMapping("/time-capsule-entries/{entry_id}")
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
            value = "TIMECAPSULE-13 타임캡슐 엔트리 삭제",
            notes = "작성자 본인의 공개 전 DRAFT 또는 SEALED 엔트리를 삭제합니다. "
                    + "DB 엔트리와 미디어는 삭제 상태로 변경하고 S3 원본 객체를 제거합니다."
    )
    @DeleteMapping("/time-capsule-entries/{entry_id}")
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
            value = "TIMECAPSULE-15 타임캡슐 엔트리 봉인",
            notes = "작성자 본인의 DRAFT 엔트리에 단일 활성 이미지가 "
                    + "등록된 경우 엔트리를 최종 봉인합니다. "
                    + "봉인된 엔트리는 보관함 목록·캘린더에 노출되며 "
                    + "이후 내용과 미디어를 수정할 수 없습니다."
    )
    @PatchMapping("/time-capsule-entries/{entry_id}/seal")
    public ResponseEntity<TimeCapsuleEntrySealResponse>
    sealTimeCapsuleEntry(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(
                    value = "타임캡슐 엔트리 ID",
                    required = true
            )
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
            value = "TIMECAPSULE-7 타임캡슐 대표 이미지 업로드 URL 발급",
            notes = "작성자 본인의 DRAFT 엔트리에 JPEG·PNG·WebP 대표 이미지 한 장을 업로드할 수 있는 15분 유효 S3 Presigned PUT URL을 발급합니다. 별도 썸네일 파일은 생성하지 않습니다."
    )
    @PostMapping("/time-capsule-entries/{entry_id}/media/upload-url")
    public ResponseEntity<CreateTimeCapsuleMediaUploadUrlResponse>
    createMediaUploadUrl(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader,
            @ApiParam(value = "타임캡슐 엔트리 ID", required = true)
            @PathVariable("entry_id")
            long timeCapsuleEntryId,
            @Valid @RequestBody CreateTimeCapsuleMediaUploadUrlRequest request
    ) {
        long memberId = accessTokenMemberResolver.resolveMemberId(
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                timeCapsuleEntryService.createMediaUploadUrl(
                        memberId,
                        timeCapsuleEntryId,
                        request
                )
        );
    }

    @ApiOperation(
            value = "TIMECAPSULE-8 타임캡슐 단일 이미지 업로드 완료",
            notes = "작성자 본인의 DRAFT 엔트리에 속한 단일 이미지의 S3 객체 존재 여부, MIME 타입, 파일 크기를 검증한 뒤 활성화합니다. 이미 활성화된 동일 이미지 요청은 멱등 성공으로 처리하며 엔트리 봉인은 별도 API에서 수행합니다."
    )
    @PostMapping("/time-capsule-entries/{entry_id}/media/complete")
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
