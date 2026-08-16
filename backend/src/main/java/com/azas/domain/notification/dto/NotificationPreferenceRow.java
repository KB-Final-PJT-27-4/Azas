package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRow {

    private NotificationCategory notificationCategory;

    private boolean enabled;
}