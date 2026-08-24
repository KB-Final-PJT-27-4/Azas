package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.*;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.entity.NotificationType;
import com.azas.domain.notification.mapper.NotificationMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int RECOMMENDED_POLL_INTERVAL_SECONDS = 5;

    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(
            Long memberId,
            Long childId,
            String categoryValue,
            String notificationTypeValue,
            String isReadValue,
            String cursorValue,
            String afterIdValue,
            String sizeValue
    ) {
        if (cursorValue != null && afterIdValue != null) {
            throw invalidQuery();
        }

        if (childId != null && childId <= 0) {
            throw invalidQuery();
        }

        NotificationCategory category =
                parseCategory(categoryValue);

        NotificationType notificationType =
                parseNotificationType(notificationTypeValue);

        validateCategoryAndType(
                category,
                notificationType
        );

        Boolean isRead = parseBoolean(isReadValue);
        Long cursorId = parsePositiveId(cursorValue, false);
        Long afterId = parsePositiveId(afterIdValue, true);
        int pageSize = parseSize(sizeValue);

        NotificationListQuery query =
                new NotificationListQuery(
                        memberId,
                        childId,
                        category,
                        notificationType,
                        isRead,
                        cursorId,
                        afterId,
                        pageSize + 1
                );

        List<NotificationListRow> pageRows =
                new ArrayList<>(
                        notificationMapper.findNotifications(query)
                );

        boolean polling = afterId != null;
        boolean hasExtra = pageRows.size() > pageSize;

        if (hasExtra) {
            pageRows.remove(pageRows.size() - 1);
        }

        Long nextCursor = null;
        Long pollCursor = null;
        boolean hasNext = false;
        boolean hasMoreNew = false;

        if (polling) {
            hasMoreNew = hasExtra;

            // SQL 결과가 ASC이므로 마지막 값이 이번에 확인한
            // 가장 큰 notification_id이다.
            if (!pageRows.isEmpty()) {
                pollCursor = pageRows
                        .get(pageRows.size() - 1)
                        .getNotificationId();
            } else {
                pollCursor = afterId;
            }

            // 실제 목록 응답은 최신 알림이 먼저 나오도록 변경한다.
            Collections.reverse(pageRows);
        } else {
            hasNext = hasExtra;

            if (cursorId == null) {
                // 알림이 아직 없는 회원도 after_id=0으로 바로 폴링을
                // 시작할 수 있게 최초 커서를 항상 제공한다.
                pollCursor = pageRows.isEmpty()
                        ? 0L
                        : pageRows.get(0).getNotificationId();
            }

            if (!pageRows.isEmpty()) {
                if (hasNext) {
                    nextCursor = pageRows
                            .get(pageRows.size() - 1)
                            .getNotificationId();
                }
            }
        }

        List<NotificationListItemResponse> items =
                pageRows.stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

        long unreadCount =
                notificationMapper.countUnreadNotifications(memberId);

        return new NotificationListResponse(
                items,
                nextCursor,
                hasNext,
                pollCursor,
                hasMoreNew,
                unreadCount,
                RECOMMENDED_POLL_INTERVAL_SECONDS
        );
    }

    private NotificationListItemResponse toResponse(
            NotificationListRow row
    ) {
        return new NotificationListItemResponse(
                row.getNotificationId(),
                row.getChildId(),
                row.getNotificationCategory(),
                row.getNotificationType(),
                row.getTitle(),
                row.getContent(),
                row.getReferenceType(),
                row.getReferenceId(),
                parseMetadata(row.getMetadataJson()),
                row.isRead(),
                row.getCreatedAt()
        );
    }

    private JsonNode parseMetadata(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    private NotificationCategory parseCategory(String value) {
        if (value == null) {
            return null;
        }

        if (value.isBlank()) {
            throw invalidQuery();
        }

        try {
            return NotificationCategory.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw invalidQuery();
        }
    }

    private NotificationType parseNotificationType(String value) {
        if (value == null) {
            return null;
        }

        if (value.isBlank()) {
            throw invalidQuery();
        }

        try {
            return NotificationType.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw invalidQuery();
        }
    }

    private void validateCategoryAndType(
            NotificationCategory category,
            NotificationType notificationType
    ) {
        if (
                category != null
                        && notificationType != null
                        && notificationType.getCategory() != category
        ) {
            throw invalidQuery();
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw invalidQuery();
    }

    private Long parsePositiveId(
            String value,
            boolean allowZero
    ) {
        if (value == null) {
            return null;
        }

        try {
            long parsed = Long.parseLong(value);

            if (parsed < 0 || (!allowZero && parsed == 0)) {
                throw new NumberFormatException();
            }

            return parsed;
        } catch (NumberFormatException exception) {
            throw invalidQuery();
        }
    }

    private int parseSize(String value) {
        if (value == null) {
            return DEFAULT_SIZE;
        }

        try {
            int size = Integer.parseInt(value);

            if (size < 1 || size > MAX_SIZE) {
                throw new NumberFormatException();
            }

            return size;
        } catch (NumberFormatException exception) {
            throw invalidQuery();
        }
    }

    private BusinessException invalidQuery() {
        return new BusinessException(
                ErrorCode.INVALID_QUERY_PARAMETER
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse getUnreadCount(
            Long memberId
    ) {
        long unreadCount =
                notificationMapper.countUnreadNotifications(
                        memberId
                );

        return new NotificationUnreadCountResponse(
                unreadCount
        );
    }

    @Override
    @Transactional
    public NotificationReadResponse readNotification(
            Long memberId,
            Long notificationId
    ) {
        if (notificationId == null || notificationId <= 0) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        Boolean readStatus =
                notificationMapper.findNotificationReadStatus(
                        memberId,
                        notificationId
                );

        if (readStatus == null) {
            throw new BusinessException(
                    ErrorCode.NOTIFICATION_NOT_FOUND
            );
        }

        if (!readStatus) {
            notificationMapper.markNotificationAsRead(
                    memberId,
                    notificationId
            );
        }

        return new NotificationReadResponse(
                notificationId,
                true
        );
    }

    @Override
    @Transactional
    public NotificationReadAllResponse readAllNotifications(
            Long memberId
    ) {
        int updatedCount =
                notificationMapper.markAllNotificationsAsRead(
                        memberId
                );

        return new NotificationReadAllResponse(
                updatedCount,
                0L
        );
    }
}
