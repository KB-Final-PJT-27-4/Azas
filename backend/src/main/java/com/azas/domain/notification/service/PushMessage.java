package com.azas.domain.notification.service;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class PushMessage {

    private final String title;
    private final String body;
    private final String actionUrl;
    private final Map<String, String> data;

    public PushMessage(
            String title,
            String body,
            String actionUrl,
            Map<String, String> data
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "푸시 알림 제목이 필요합니다."
            );
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException(
                    "푸시 알림 내용이 필요합니다."
            );
        }

        this.title = title.trim();
        this.body = body.trim();
        this.actionUrl = normalizeActionUrl(actionUrl);
        this.data = immutableData(data);
    }

    private String normalizeActionUrl(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return null;
        }
        return actionUrl.trim();
    }

    private Map<String, String> immutableData(
            Map<String, String> data
    ) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> copied = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (
                    key != null
                            && !key.isBlank()
                            && value != null
            ) {
                copied.put(key, value);
            }
        });
        return Collections.unmodifiableMap(copied);
    }
}
