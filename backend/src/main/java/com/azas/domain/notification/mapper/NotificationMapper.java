package com.azas.domain.notification.mapper;

import com.azas.domain.notification.dto.NotificationListQuery;
import com.azas.domain.notification.dto.NotificationListRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    List<NotificationListRow> findNotifications(
            NotificationListQuery query
    );

    long countUnreadNotifications(
            @Param("memberId") Long memberId
    );
}