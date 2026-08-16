package com.azas.domain.notification.service;

import com.azas.domain.notification.dto.NotificationPreferenceItemResponse;
import com.azas.domain.notification.dto.NotificationPreferenceListResponse;
import com.azas.domain.notification.dto.NotificationPreferenceRow;
import com.azas.domain.notification.entity.NotificationCategory;
import com.azas.domain.notification.mapper.NotificationPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl
        implements NotificationPreferenceService {

    private final NotificationPreferenceMapper
            notificationPreferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceListResponse
    getNotificationPreferences(Long memberId) {

        List<NotificationPreferenceRow> rows =
                notificationPreferenceMapper
                        .findNotificationPreferences(memberId);

        Map<NotificationCategory, Boolean> enabledByCategory =
                new EnumMap<>(NotificationCategory.class);

        for (NotificationPreferenceRow row : rows) {
            enabledByCategory.put(
                    row.getNotificationCategory(),
                    row.isEnabled()
            );
        }

        List<NotificationPreferenceItemResponse> items =
                Arrays.stream(NotificationCategory.values())
                        .sorted(
                                Comparator.comparingInt(
                                        NotificationCategory
                                                ::getDisplayOrder
                                )
                        )
                        .map(category ->
                                new NotificationPreferenceItemResponse(
                                        category,
                                        category.getLabel(),
                                        category.getDescription(),
                                        enabledByCategory
                                                .getOrDefault(
                                                        category,
                                                        true
                                                )
                                )
                        )
                        .collect(Collectors.toList());

        return new NotificationPreferenceListResponse(
                items
        );
    }
}