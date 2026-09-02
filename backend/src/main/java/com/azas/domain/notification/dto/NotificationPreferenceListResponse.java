package com.azas.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationPreferenceListResponse {

    private final List<NotificationPreferenceItemResponse> items;
}