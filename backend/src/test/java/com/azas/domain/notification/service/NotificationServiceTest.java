package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationListResponse;
import com.azas.domain.notification.dto.NotificationListRow;
import com.azas.domain.notification.dto.NotificationReadResponse;
import com.azas.domain.notification.dto.NotificationUnreadCountResponse;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import com.azas.domain.notification.mapper.NotificationMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private NotificationMapper notificationMapper;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationMapper,
                new ObjectMapper()
        );
    }

    // 초기 알림 목록 조회 페이지 테스트
    // 설명 : 커서나 afterId 없이 처음으로 알림 목록을 요청했을 때,
    // 요청한 size 크기만큼 데이터가 잘 잘려서(paging) 오는지,
    // pollCursor(최신 알림 기준점)와 nextCursor(다음 페이지용 커서) 등이 올바르게 세팅되는지 확인합니다.
    @Test
    void getsInitialNotificationPage() {
        when(notificationMapper.findNotifications(any()))
                .thenReturn(List.of(
                        row(10L),
                        row(9L),
                        row(8L)
                ));

        when(notificationMapper.countUnreadNotifications(MEMBER_ID))
                .thenReturn(3L);

        NotificationListResponse response =
                notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "2"
                );

        assertEquals(2, response.getItems().size());
        assertEquals(10L, response.getPollCursor());
        assertEquals(9L, response.getNextCursor());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasMoreNew());
        assertEquals(3L, response.getUnreadCount());
    }

    // 신규 알림 폴링(실시간 갱신) 테스트 (중간 누락 없음)
    // 설명: afterId(이전까지 받았던 마지막 알림 ID)를 전달하여 그 이후에 발생한 새로운 알림들을 가져올 때,
    // 아이디가 누락되지 않고 올바르게 최신순으로 정렬되어 반환되는지 확인합니다.
    @Test
    void getsNewNotificationsWithoutSkippingIds() {
        // 폴링 SQL 결과는 ASC
        when(notificationMapper.findNotifications(any()))
                .thenReturn(List.of(
                        row(11L),
                        row(12L),
                        row(13L)
                ));

        when(notificationMapper.countUnreadNotifications(MEMBER_ID))
                .thenReturn(3L);

        NotificationListResponse response =
                notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "10",
                        "2"
                );

        // 응답은 최신순으로 다시 정렬
        assertEquals(
                12L,
                response.getItems().get(0).getNotificationId()
        );
        assertEquals(
                11L,
                response.getItems().get(1).getNotificationId()
        );

        assertEquals(12L, response.getPollCursor());
        assertTrue(response.isHasMoreNew());
        assertFalse(response.isHasNext());
        assertNull(response.getNextCursor());
    }

    // cursor와 afterId 동시 요청 예외 처리 테스트
    // 설명 : 과거 페이지 조회를 위한 cursor와 신규 알림 폴링을 위한 afterId는 동시에 사용할 수 없도록 되어 있습니다.
    // 둘 다 같이 전달했을 때 INVALID_QUERY_PARAMETER 예외가 발생하는지 검증
    @Test
    void rejectsCursorAndAfterIdTogether() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null,
                        "10",
                        "20",
                        null
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }

    //카테고리와 알림 타입이 불일치할 때 예외 처리 테스트
    // 설명: 예를 들어 카테고리는 ALLOWANCE(용돈)인데 알림 타입은 MISSION_APPROVED(미션 승인)처럼
    // 서로 맞지 않는 조합으로 요청했을 때, 올바르게 INVALID_QUERY_PARAMETER 예외를 뱉어내는지 확인합니다.
    @Test
    void rejectsMismatchedCategoryAndType() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        "ALLOWANCE",
                        "MISSION_APPROVED",
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }

    private NotificationListRow row(long notificationId) {
        return new NotificationListRow(
                notificationId,
                3L,
                NotificationCategory.ALLOWANCE,
                NotificationType.ALLOWANCE_REQUESTED,
                "깨비가 용돈을 요청했어요",
                "10,000원이 필요한 이유를 확인해 주세요.",
                "ALLOWANCE_REQUEST",
                41L,
                "{\"requested_amount\":10000}",
                false,
                LocalDateTime.of(
                        2026,
                        8,
                        16,
                        10,
                        20
                )
        );
    }

    // 신규 알림이 없을 때의 폴링 테스트
    // 설명: afterId를 주고 신규 알림을 요청했는데 새로운 알림이 하나도 없을 경우,
    // 빈 리스트를 반환하면서 기존에 가지고 있던 pollCursor 값을 유지하여 내려주는지 확인합니다.
    @Test
    void returnsSamePollCursorWhenNoNewNotificationExists() {
        when(notificationMapper.findNotifications(any()))
                .thenReturn(List.of());

        when(notificationMapper.countUnreadNotifications(MEMBER_ID))
                .thenReturn(2L);

        NotificationListResponse response =
                notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "10",
                        "20"
                );

        assertTrue(response.getItems().isEmpty());
        assertEquals(10L, response.getPollCursor());
        assertFalse(response.isHasMoreNew());
        assertFalse(response.isHasNext());
        assertNull(response.getNextCursor());
        assertEquals(2L, response.getUnreadCount());
    }

    // 과거 페이지네이션 조회 테스트
    // 설명: cursor 파라미터를 사용하여 이전(더 오래된) 알림 목록을 페이지네이션으로 불러올 때,
    // 알림 리스트가 정확한 순서로 잘 담겨 오는지, 그리고 과거 페이지 조회 시에는 실시간 폴링용 pollCursor가 포함되지 않고 null이 되는지 검증합니다.
    @Test
    void getsOlderNotificationsUsingCursor() {
        when(notificationMapper.findNotifications(any()))
                .thenReturn(List.of(
                        row(9L),
                        row(8L),
                        row(7L)
                ));

        when(notificationMapper.countUnreadNotifications(MEMBER_ID))
                .thenReturn(1L);

        NotificationListResponse response =
                notificationService.getNotifications(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null,
                        "10",
                        null,
                        "2"
                );

        assertEquals(2, response.getItems().size());
        assertEquals(9L, response.getItems().get(0)
                .getNotificationId());
        assertEquals(8L, response.getItems().get(1)
                .getNotificationId());

        assertEquals(8L, response.getNextCursor());
        assertTrue(response.isHasNext());

        // 과거 페이지 응답으로 기존 폴링 커서를 덮어쓰지 않음
        assertNull(response.getPollCursor());
        assertFalse(response.isHasMoreNew());
    }

    // 잘못된 쿼리 파라미터(유효성 검증) 테스트
    // 설명: 잘못된 값(0 이하의 커서/아이디, 허용 범위를 벗어난 size, 잘못된 isRead 값, 알 수 없는 category 등)을 넣었을 때
    // 어김없이 INVALID_QUERY_PARAMETER 예외를 발생시키는지 여러 케이스를 한 번에 검증합니다.
    @Test
    void rejectsInvalidQueryParameters() {
        assertInvalidQuery(null, null, null, null, null, "0", null, null);
        assertInvalidQuery(null, null, null, null, null, null, "-1", null);
        assertInvalidQuery(null, null, null, null, null, null, null, "0");
        assertInvalidQuery(null, null, null, null, null, null, null, "101");

        assertInvalidQuery(
                null,
                null,
                null,
                "yes",
                null,
                null,
                null,
                null
        );

        assertInvalidQuery(
                null,
                "UNKNOWN",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private void assertInvalidQuery(
            Long childId,
            String category,
            String notificationType,
            String isRead,
            String unused,
            String cursor,
            String afterId,
            String size
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> notificationService.getNotifications(
                        MEMBER_ID,
                        childId,
                        category,
                        notificationType,
                        isRead,
                        cursor,
                        afterId,
                        size
                )
        );

        assertEquals(
                ErrorCode.INVALID_QUERY_PARAMETER,
                exception.getErrorCode()
        );
    }

    // 읽지 않은 알림 수 테스트
    @Test
    void getsUnreadNotificationCount() {
        when(notificationMapper.countUnreadNotifications(
                MEMBER_ID
        )).thenReturn(3L);

        NotificationUnreadCountResponse response =
                notificationService.getUnreadCount(
                        MEMBER_ID
                );

        assertEquals(3L, response.getUnreadCount());

        verify(notificationMapper)
                .countUnreadNotifications(MEMBER_ID);
    }

    // 0건 테스트
    @Test
    void returnsZeroWhenNoUnreadNotificationsExist() {
        when(notificationMapper.countUnreadNotifications(
                MEMBER_ID
        )).thenReturn(0L);

        NotificationUnreadCountResponse response =
                notificationService.getUnreadCount(
                        MEMBER_ID
                );

        assertEquals(0L, response.getUnreadCount());
    }

    // 읽지 않은 알림 읽음
    @Test
    void readsUnreadNotification() {
        Long memberId = 7L;
        Long notificationId = 101L;

        when(notificationMapper.findNotificationReadStatus(
                memberId,
                notificationId
        )).thenReturn(false);

        NotificationReadResponse response =
                notificationService.readNotification(
                        memberId,
                        notificationId
                );

        assertEquals(notificationId, response.getNotificationId());
        assertTrue(response.isRead());

        verify(notificationMapper)
                .markNotificationAsRead(
                        memberId,
                        notificationId
                );
    }

    // 이미 읽은 알림 재요청
    @Test
    void returnsSuccessWhenNotificationIsAlreadyRead() {
        Long memberId = 7L;
        Long notificationId = 101L;

        when(notificationMapper.findNotificationReadStatus(
                memberId,
                notificationId
        )).thenReturn(true);

        NotificationReadResponse response =
                notificationService.readNotification(
                        memberId,
                        notificationId
                );

        assertEquals(notificationId, response.getNotificationId());
        assertTrue(response.isRead());

        verify(notificationMapper, never())
                .markNotificationAsRead(
                        memberId,
                        notificationId
                );
    }

    // 알림을 찾을 수 없는 경우
    @Test
    void throwsNotFoundWhenNotificationDoesNotBelongToMember() {
        Long memberId = 7L;
        Long notificationId = 999L;

        when(notificationMapper.findNotificationReadStatus(
                memberId,
                notificationId
        )).thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> notificationService.readNotification(
                                memberId,
                                notificationId
                        )
                );

        assertEquals(
                ErrorCode.NOTIFICATION_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(notificationMapper, never())
                .markNotificationAsRead(
                        memberId,
                        notificationId
                );
    }

    // 잘못된 알림 ID
    @Test
    void throwsBadRequestWhenNotificationIdIsNotPositive() {
        Long memberId = 7L;

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> notificationService.readNotification(
                                memberId,
                                0L
                        )
                );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
    }
}