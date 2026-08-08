package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleExportRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportDownloadUrlResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleExport;
import com.azas.domain.timecapsule.entity.TimeCapsuleExportType;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleExportMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimeCapsuleExportService {

    private static final Duration DOWNLOAD_URL_VALIDITY = Duration.ofMinutes(10);

    private final TimeCapsuleMapper timeCapsuleMapper;
    private final TimeCapsuleEntryMapper timeCapsuleEntryMapper;
    private final TimeCapsuleExportMapper timeCapsuleExportMapper;
    private final TimeCapsuleObjectStorage timeCapsuleObjectStorage;
    private final ObjectMapper objectMapper;

    @Transactional
    // [JMG] CAPSULE-11 공개 보관함의 봉인 기록을 결과물 생성 워커가 처리할 PENDING 작업으로 등록한다.
    public TimeCapsuleExportResponse createTimeCapsuleExport(
            long requesterMemberId,
            long timeCapsuleId,
            CreateTimeCapsuleExportRequest request
    ) {
        TimeCapsule timeCapsule = getReleasedTimeCapsuleForUpdateOrThrow(
                requesterMemberId,
                timeCapsuleId
        );
        if (timeCapsuleEntryMapper.countSealedByTimeCapsuleId(
                timeCapsuleId
        ) < 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_EXPORT_CREATION_NOT_ALLOWED
            );
        }

        TimeCapsuleExport export = TimeCapsuleExport.createPending(
                timeCapsuleId,
                requesterMemberId,
                TimeCapsuleExportType.from(request.getExportType()),
                serializeOptions(request.getOptions())
        );
        if (timeCapsuleExportMapper.insert(export) != 1) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_EXPORT_CREATION_NOT_ALLOWED
            );
        }

        return TimeCapsuleExportResponse.from(
                getAccessibleExportOrThrow(
                        requesterMemberId,
                        export.getTimeCapsuleExportId()
                )
        );
    }

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-9 부모·보호자 권한을 확인한 뒤 결과물 생성 작업 상태와 메타데이터를 조회한다.
    public TimeCapsuleExportResponse getTimeCapsuleExport(
            long requesterMemberId,
            long timeCapsuleExportId
    ) {
        return TimeCapsuleExportResponse.from(
                getAccessibleExportOrThrow(
                        requesterMemberId,
                        timeCapsuleExportId
                )
        );
    }

    @Transactional(readOnly = true)
    // [JMG] CAPSULE-10 완료·미만료 결과물에 한해 비공개 객체의 짧은 수명 다운로드 URL을 발급한다.
    public TimeCapsuleExportDownloadUrlResponse
    createTimeCapsuleExportDownloadUrl(
            long requesterMemberId,
            long timeCapsuleExportId
    ) {
        TimeCapsuleExport export = getAccessibleExportOrThrow(
                requesterMemberId,
                timeCapsuleExportId
        );
        if (!export.isSucceeded()
                || export.getOutputObjectKey() == null
                || export.getOutputObjectKey().isBlank()) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_EXPORT_NOT_READY);
        }
        if (export.isExpiredAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_EXPORT_EXPIRED);
        }

        TimeCapsuleObjectStorage.PresignedUrl presignedUrl =
                timeCapsuleObjectStorage.createDownloadUrl(
                        export.getOutputObjectKey(),
                        DOWNLOAD_URL_VALIDITY
                );
        return new TimeCapsuleExportDownloadUrlResponse(
                export.getTimeCapsuleExportId(),
                presignedUrl.url(),
                DOWNLOAD_URL_VALIDITY.toSeconds()
        );
    }

    // [JMG] CAPSULE-11 삭제와 결과물 요청이 충돌하지 않도록 공개된 보관함 행을 잠근다.
    private TimeCapsule getReleasedTimeCapsuleForUpdateOrThrow(
            long requesterMemberId,
            long timeCapsuleId
    ) {
        TimeCapsule timeCapsule =
                timeCapsuleMapper.findAccessibleByIdForUpdate(
                        timeCapsuleId,
                        requesterMemberId
                );
        if (timeCapsule == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_NOT_FOUND);
        }
        if (timeCapsule.getStatus() != TimeCapsuleStatus.RELEASED) {
            throw new BusinessException(
                    ErrorCode.TIME_CAPSULE_EXPORT_CREATION_NOT_ALLOWED
            );
        }

        return timeCapsule;
    }

    // [JMG] CAPSULE-9·10 부모·보호자 관계로 보호된 결과물 작업만 반환해 존재 여부를 노출하지 않는다.
    private TimeCapsuleExport getAccessibleExportOrThrow(
            long requesterMemberId,
            long timeCapsuleExportId
    ) {
        TimeCapsuleExport export = timeCapsuleExportMapper.findAccessibleById(
                timeCapsuleExportId,
                requesterMemberId
        );
        if (export == null) {
            throw new BusinessException(ErrorCode.TIME_CAPSULE_EXPORT_NOT_FOUND);
        }

        return export;
    }

    // [JMG] CAPSULE-11 유연한 결과물 옵션을 파라미터 바인딩 가능한 JSON 문자열로 직렬화한다.
    private String serializeOptions(Map<String, Object> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.BADREQUEST, exception);
        }
    }
}
