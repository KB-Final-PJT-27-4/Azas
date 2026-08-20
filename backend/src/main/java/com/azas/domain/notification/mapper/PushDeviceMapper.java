package com.azas.domain.notification.mapper;

import com.azas.domain.notification.dto.PushDeviceCommand;
import com.azas.domain.notification.dto.PushDeviceRow;
import com.azas.domain.notification.dto.ActivePushDeviceRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PushDeviceMapper {

    PushDeviceRow findByMemberAndDeviceKey(
            @Param("memberId") Long memberId,
            @Param("deviceKey") String deviceKey
    );

    int deactivateByTokenHashExceptDevice(
            @Param("tokenHash") String tokenHash,
            @Param("memberId") Long memberId,
            @Param("deviceKey") String deviceKey
    );

    int upsert(PushDeviceCommand command);

    int deactivate(
            @Param("pushDeviceId") Long pushDeviceId,
            @Param("memberId") Long memberId
    );

    List<ActivePushDeviceRow> findActiveByMemberId(
            @Param("memberId") Long memberId
    );

    int deactivateById(
            @Param("pushDeviceId") Long pushDeviceId
    );
}
