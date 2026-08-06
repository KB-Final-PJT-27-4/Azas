package com.azas.domain.timecapsule.service;

import com.azas.domain.timecapsule.dto.CreateTimeCapsuleExportRequest;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportDownloadUrlResponse;
import com.azas.domain.timecapsule.dto.TimeCapsuleExportResponse;
import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.azas.domain.timecapsule.entity.TimeCapsuleExport;
import com.azas.domain.timecapsule.entity.TimeCapsuleExportStatus;
import com.azas.domain.timecapsule.entity.TimeCapsuleExportType;
import com.azas.domain.timecapsule.entity.TimeCapsuleStatus;
import com.azas.domain.timecapsule.mapper.TimeCapsuleEntryMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleExportMapper;
import com.azas.domain.timecapsule.mapper.TimeCapsuleMapper;
import com.azas.domain.timecapsule.storage.TimeCapsuleObjectStorage;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeCapsuleExportServiceTest {

    @Mock
    private TimeCapsuleMapper timeCapsuleMapper;

    @Mock
    private TimeCapsuleEntryMapper timeCapsuleEntryMapper;

    @Mock
    private TimeCapsuleExportMapper timeCapsuleExportMapper;

    @Mock
    private TimeCapsuleObjectStorage timeCapsuleObjectStorage;

    private TimeCapsuleExportService timeCapsuleExportService;

    @BeforeEach
    // [JMG] CAPSULE-9~11 실제 JSON 직렬화기를 사용해 결과물 서비스 단위 테스트를 구성한다.
    void setUp() {
        timeCapsuleExportService = new TimeCapsuleExportService(
                timeCapsuleMapper,
                timeCapsuleEntryMapper,
                timeCapsuleExportMapper,
                timeCapsuleObjectStorage,
                new ObjectMapper()
        );
    }

    @Test
    // [JMG] CAPSULE-11 공개됐고 봉인 엔트리가 있는 보관함은 PENDING 결과물 작업을 생성한다.
    void createTimeCapsuleExportCreatesPendingJob() {
        TimeCapsule timeCapsule = createTimeCapsule(
                100L,
                TimeCapsuleStatus.RELEASED
        );
        TimeCapsuleExport persistedExport = createExport(
                1101L,
                100L,
                TimeCapsuleExportType.VIDEO,
                TimeCapsuleExportStatus.PENDING
        );
        ReflectionTestUtils.setField(
                persistedExport,
                "createdAt",
                LocalDateTime.of(2026, 8, 5, 12, 0)
        );
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(timeCapsule);
        given(timeCapsuleEntryMapper.countSealedByTimeCapsuleId(100L))
                .willReturn(1);
        doAnswer(invocation -> {
            TimeCapsuleExport export = invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    export,
                    "timeCapsuleExportId",
                    1101L
            );
            return 1;
        }).when(timeCapsuleExportMapper).insert(any(TimeCapsuleExport.class));
        given(timeCapsuleExportMapper.findAccessibleById(1101L, 7L))
                .willReturn(persistedExport);

        TimeCapsuleExportResponse response =
                timeCapsuleExportService.createTimeCapsuleExport(
                        7L,
                        100L,
                        createExportRequest(
                                "VIDEO",
                                Map.of("include_background_music", true)
                        )
                );

        ArgumentCaptor<TimeCapsuleExport> captor =
                ArgumentCaptor.forClass(TimeCapsuleExport.class);
        verify(timeCapsuleExportMapper).insert(captor.capture());
        assertEquals(TimeCapsuleExportType.VIDEO,
                captor.getValue().getExportType());
        assertEquals("PENDING", captor.getValue().getStatus().name());
        assertEquals("{\"include_background_music\":true}",
                captor.getValue().getOptionsJson());
        assertEquals(1101L, response.getTimeCapsuleExportId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    // [JMG] CAPSULE-11 봉인 엔트리가 하나도 없으면 공개 보관함에서도 결과물 작업을 생성하지 않는다.
    void createTimeCapsuleExportRejectsWhenNoSealedEntryExists() {
        given(timeCapsuleMapper.findAccessibleByIdForUpdate(100L, 7L))
                .willReturn(createTimeCapsule(
                        100L,
                        TimeCapsuleStatus.RELEASED
                ));
        given(timeCapsuleEntryMapper.countSealedByTimeCapsuleId(100L))
                .willReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleExportService.createTimeCapsuleExport(
                        7L,
                        100L,
                        createExportRequest("ARCHIVE", Map.of())
                )
        );

        assertEquals(ErrorCode.TIME_CAPSULE_EXPORT_CREATION_NOT_ALLOWED,
                exception.getErrorCode());
        verify(timeCapsuleExportMapper, never()).insert(
                any(TimeCapsuleExport.class)
        );
    }

    @Test
    // [JMG] CAPSULE-10 완료되고 미만료인 결과물은 객체 키 대신 Presigned GET URL만 반환한다.
    void createTimeCapsuleExportDownloadUrlReturnsPresignedUrl() {
        TimeCapsuleExport export = createExport(
                1101L,
                100L,
                TimeCapsuleExportType.VIDEO,
                TimeCapsuleExportStatus.SUCCEEDED
        );
        ReflectionTestUtils.setField(
                export,
                "outputObjectKey",
                "time-capsules/100/exports/1101.mp4"
        );
        ReflectionTestUtils.setField(
                export,
                "expiresAt",
                LocalDateTime.now().plusDays(1)
        );
        given(timeCapsuleExportMapper.findAccessibleById(1101L, 7L))
                .willReturn(export);
        given(timeCapsuleObjectStorage.createDownloadUrl(
                eq("time-capsules/100/exports/1101.mp4"),
                any()
        )).willReturn(new TimeCapsuleObjectStorage.PresignedUrl(
                "https://s3.example.test/presigned-export-download"
        ));

        TimeCapsuleExportDownloadUrlResponse response =
                timeCapsuleExportService.createTimeCapsuleExportDownloadUrl(
                        7L,
                        1101L
                );

        assertEquals("https://s3.example.test/presigned-export-download",
                response.getDownloadUrl());
        assertEquals(600L, response.getExpiresInSeconds());
    }

    @Test
    // [JMG] CAPSULE-10 보관 기간이 만료된 결과물은 다운로드 URL 발급을 차단한다.
    void createTimeCapsuleExportDownloadUrlRejectsExpiredExport() {
        TimeCapsuleExport export = createExport(
                1101L,
                100L,
                TimeCapsuleExportType.ARCHIVE,
                TimeCapsuleExportStatus.SUCCEEDED
        );
        ReflectionTestUtils.setField(
                export,
                "outputObjectKey",
                "time-capsules/100/exports/1101.zip"
        );
        ReflectionTestUtils.setField(
                export,
                "expiresAt",
                LocalDateTime.now().minusSeconds(1)
        );
        given(timeCapsuleExportMapper.findAccessibleById(1101L, 7L))
                .willReturn(export);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> timeCapsuleExportService
                        .createTimeCapsuleExportDownloadUrl(7L, 1101L)
        );

        assertEquals(ErrorCode.TIME_CAPSULE_EXPORT_EXPIRED,
                exception.getErrorCode());
        verify(timeCapsuleObjectStorage, never()).createDownloadUrl(
                any(),
                any()
        );
    }

    // [JMG] CAPSULE-11 테스트용 공개·수집 상태 타임캡슐 엔티티를 구성한다.
    private TimeCapsule createTimeCapsule(
            long timeCapsuleId,
            TimeCapsuleStatus status
    ) {
        TimeCapsule timeCapsule = TimeCapsule.create(
                10L,
                4L,
                "깨비의 대학자금 타임캡슐",
                LocalDate.of(2030, 7, 23)
        );
        ReflectionTestUtils.setField(
                timeCapsule,
                "timeCapsuleId",
                timeCapsuleId
        );
        ReflectionTestUtils.setField(timeCapsule, "status", status);
        return timeCapsule;
    }

    // [JMG] CAPSULE-9~10 테스트용 결과물 생성 작업을 상태별로 구성한다.
    private TimeCapsuleExport createExport(
            long exportId,
            long timeCapsuleId,
            TimeCapsuleExportType type,
            TimeCapsuleExportStatus status
    ) {
        TimeCapsuleExport export = TimeCapsuleExport.createPending(
                timeCapsuleId,
                7L,
                type,
                null
        );
        ReflectionTestUtils.setField(
                export,
                "timeCapsuleExportId",
                exportId
        );
        ReflectionTestUtils.setField(export, "status", status);
        return export;
    }

    // [JMG] CAPSULE-11 테스트용 결과물 생성 요청을 유형과 옵션 조합으로 구성한다.
    private CreateTimeCapsuleExportRequest createExportRequest(
            String exportType,
            Map<String, Object> options
    ) {
        CreateTimeCapsuleExportRequest request =
                new CreateTimeCapsuleExportRequest();
        ReflectionTestUtils.setField(request, "exportType", exportType);
        ReflectionTestUtils.setField(request, "options", options);
        return request;
    }
}
