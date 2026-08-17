package com.azas.domain.notification.mapper;

import com.azas.domain.notification.dto.NotificationPreferenceRow;
import com.azas.domain.notification.dto.UpdateNotificationPreferencesRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationPreferenceMapper {

    List<NotificationPreferenceRow>
    findNotificationPreferences(
            @Param("memberId") Long memberId
    );

    int upsertNotificationPreferences(
            @Param("memberId") Long memberId,
            @Param("items")
            List<UpdateNotificationPreferencesRequest.Item> items
    );
}