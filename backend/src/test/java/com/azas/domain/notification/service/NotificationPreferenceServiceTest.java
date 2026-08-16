package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.dto.NotificationPreferenceRow;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.mapper.NotificationPreferenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.azas.domain.notification.dto.UpdateNotificationPreferencesRequest;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    private static final Long MEMBER_ID = 7L;

    @Mock
    private NotificationPreferenceMapper
            notificationPreferenceMapper;

    private NotificationPreferenceService
            notificationPreferenceService;

    @BeforeEach
    void setUp() {
        notificationPreferenceService =
                new NotificationPreferenceServiceImpl(
                        notificationPreferenceMapper
                );
    }

//    테스트 내용: 알림 설정 목록 조회 및 정렬 순서 검증
//
//    설명: DB(Mapper)에 일부 카테고리 설정만 저장되어 있거나 순서가 뒤섞여 조회되더라도,
//    서비스 단에서 지정된 정렬 순서(Display Order)에 맞춰 총 6개의 카테고리 전체가 올바르게 정렬되어 반환되는지 확인합니다.
//    (예: 특정 항목은 false, 다른 항목은 true로 저장된 값이 정확히 매핑되는지 검증)
    @Test
    void getsNotificationPreferencesInDisplayOrder() {
        when(notificationPreferenceMapper
                .findNotificationPreferences(MEMBER_ID))
                .thenReturn(List.of(
                        new NotificationPreferenceRow(
                                NotificationCategory.USAGE_LIMIT,
                                false
                        ),
                        new NotificationPreferenceRow(
                                NotificationCategory.SAVINGS,
                                true
                        )
                ));

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .getNotificationPreferences(MEMBER_ID);

        assertEquals(6, response.getItems().size());

        assertEquals(
                NotificationCategory.SAVINGS,
                response.getItems()
                        .get(0)
                        .getNotificationCategory()
        );

        assertTrue(
                response.getItems()
                        .get(0)
                        .isEnabled()
        );

        assertEquals(
                NotificationCategory.USAGE_LIMIT,
                response.getItems()
                        .get(4)
                        .getNotificationCategory()
        );

        assertFalse(
                response.getItems()
                        .get(4)
                        .isEnabled()
        );

        verify(notificationPreferenceMapper)
                .findNotificationPreferences(MEMBER_ID);
    }

//    테스트 내용: 알림 설정 내역이 없을 때 기본값(모두 허용) 반환 검증
//    설명: 회원이 아직 알림 설정을 한 번도 건드리지 않아서 DB에 저장된 데이터가 아예 없는 경우(List.of() 반환),
//    시스템이 이를 기본값으로 처리하여 모든 카테고리(6개)의 수신 여부를 기본적으로 true(허용)로 설정해 반환하는지 확인합니다.
    @Test
    void returnsDefaultEnabledWhenPreferenceDoesNotExist() {
        when(notificationPreferenceMapper
                .findNotificationPreferences(MEMBER_ID))
                .thenReturn(List.of());

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .getNotificationPreferences(MEMBER_ID);

        assertEquals(6, response.getItems().size());

        assertTrue(
                response.getItems()
                        .stream()
                        .allMatch(item -> item.isEnabled())
        );
    }

//    테스트 내용: 알림 설정 변경(수정/저장) 정상 동작 검증
//    설명: 회원이 알림 수신 설정을 변경하기 위해 요청을 보냈을 때, 매퍼의 upsert 메서드가 정상적으로 호출되고,
//    변경된 결과가 반영된 최신 설정 목록이 올바르게 반환되는지 성공 케이스를 검증합니다.
    @Test
    void updatesNotificationPreferences() {
        UpdateNotificationPreferencesRequest request =
                allPreferencesRequest();

        when(notificationPreferenceMapper
                .upsertNotificationPreferences(
                        MEMBER_ID,
                        request.getItems()
                ))
                .thenReturn(6);

        when(notificationPreferenceMapper
                .findNotificationPreferences(MEMBER_ID))
                .thenReturn(
                        request.getItems()
                                .stream()
                                .map(item ->
                                        new NotificationPreferenceRow(
                                                item.getNotificationCategory(),
                                                item.getEnabled()
                                        )
                                )
                                .collect(Collectors.toList())
                );

        NotificationPreferenceListResponse response =
                notificationPreferenceService
                        .updateNotificationPreferences(
                                MEMBER_ID,
                                request
                        );

        assertEquals(6, response.getItems().size());

        verify(notificationPreferenceMapper)
                .upsertNotificationPreferences(
                        MEMBER_ID,
                        request.getItems()
                );
    }

//    테스트 내용: 중복된 알림 카테고리 요청 시 예외 처리 검증
//    설명: 알림 설정 요청 리스트 안에 동일한 카테고리(예: SAVINGS)가 두 번 이상 중복해서 들어있을 경우,
//    이를 비정상 요청으로 판단하여 INVALID_NOTIFICATION_PREFERENCES 에러를 터뜨리는지 확인합니다.
//    또한, 잘못된 요청이므로 DB 업데이트 함수(upsertNotificationPreferences)가 절대 호출되지(never()) 않는지 안전장치까지 함께 검증합니다.
    @Test
    void rejectsDuplicatedNotificationCategory() {
        UpdateNotificationPreferencesRequest request =
                new UpdateNotificationPreferencesRequest(
                        List.of(
                                item(NotificationCategory.SAVINGS, true),
                                item(NotificationCategory.SAVINGS, false),
                                item(NotificationCategory.TIME_CAPSULE, true),
                                item(NotificationCategory.ALLOWANCE, true),
                                item(NotificationCategory.PREGNANCY, true),
                                item(NotificationCategory.USAGE_LIMIT, true)
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> notificationPreferenceService
                                .updateNotificationPreferences(
                                        MEMBER_ID,
                                        request
                                )
                );

        assertEquals(
                ErrorCode.INVALID_NOTIFICATION_PREFERENCES,
                exception.getErrorCode()
        );

        verify(
                notificationPreferenceMapper,
                never()
        ).upsertNotificationPreferences(
                anyLong(),
                anyList()
        );
    }
    // 테스트 헬퍼
    private UpdateNotificationPreferencesRequest
    allPreferencesRequest() {
        List<UpdateNotificationPreferencesRequest.Item> items =
                Arrays.stream(NotificationCategory.values())
                        .map(category ->
                                item(category, true)
                        )
                        .collect(Collectors.toList());

        return new UpdateNotificationPreferencesRequest(
                items
        );
    }

    private UpdateNotificationPreferencesRequest.Item item(
            NotificationCategory category,
            boolean enabled
    ) {
        return new UpdateNotificationPreferencesRequest.Item(
                category,
                enabled
        );
    }
}