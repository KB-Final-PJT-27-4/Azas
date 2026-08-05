package com.azas.domain.timecapsule.mapper;

import com.azas.domain.timecapsule.entity.TimeCapsuleExport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeCapsuleExportMapper {

    // [JMG] CAPSULE-11 새 비동기 결과물 생성 요청을 저장하고 생성된 작업 ID를 채운다.
    int insert(TimeCapsuleExport export);

    // [JMG] CAPSULE-9·10 부모·보호자가 접근 가능한 결과물 작업 하나를 조회한다.
    TimeCapsuleExport findAccessibleById(
            @Param("timeCapsuleExportId") long timeCapsuleExportId,
            @Param("memberId") long memberId
    );

    // [JMG] CAPSULE-6 보관함 삭제 시 결과물 S3 객체 키를 잠금과 함께 조회한다.
    List<String> findOutputObjectKeysByTimeCapsuleIdForUpdate(
            @Param("timeCapsuleId") long timeCapsuleId
    );

    // [JMG] CAPSULE-6 보관함 영구 삭제 전 결과물 생성 이력을 삭제한다.
    int deleteByTimeCapsuleId(@Param("timeCapsuleId") long timeCapsuleId);
}
