package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.child.entity.RelationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface FamilyInvitationMapper {

    FamilyInvitation findByInviteTokenHash(
            @Param("inviteTokenHash")
            String inviteTokenHash
    );

    int acceptIfPending(
            @Param("familyInvitationId")
            Long familyInvitationId,

            @Param("acceptedMemberId")
            Long acceptedMemberId,

            @Param("relationType")
            RelationType relationType,

            @Param("acceptedAt")
            LocalDateTime acceptedAt
    );
}