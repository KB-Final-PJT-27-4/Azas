package com.azas.domain.notification.mapper;

import com.azas.domain.notification.dto.NotificationInsertCommand;
import com.azas.domain.notification.dto.TimeCapsuleReleaseNotificationTarget;
import com.azas.domain.notification.dto.UsageLimitNotificationTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProactiveNotificationMapper {

    List<UsageLimitNotificationTarget> findUsageLimitTargets(
            @Param("startOccurredAt") LocalDateTime startOccurredAt,
            @Param("endOccurredAtExclusive") LocalDateTime endOccurredAtExclusive
    );

    List<TimeCapsuleReleaseNotificationTarget> findDueTimeCapsules(
            @Param("now") LocalDateTime now
    );

    List<Long> findEnabledParentRecipientIds(
            @Param("childId") long childId,
            @Param("category") String category
    );

    int insertNotification(NotificationInsertCommand command);
}
