package com.azas.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationInsertCommand {
    private final Long memberId;
    private final Long childId;
    private final String category;
    private final String type;
    private final String title;
    private final String content;
    private final String referenceType;
    private final Long referenceId;
    private final String metadataJson;
    private final String deduplicationKey;
    private final LocalDateTime createdAt;
}
