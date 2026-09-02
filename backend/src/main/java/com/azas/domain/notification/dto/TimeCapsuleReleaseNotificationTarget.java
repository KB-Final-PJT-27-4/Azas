package com.azas.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeCapsuleReleaseNotificationTarget {
    private Long timeCapsuleId;
    private Long childId;
    private String title;
}
