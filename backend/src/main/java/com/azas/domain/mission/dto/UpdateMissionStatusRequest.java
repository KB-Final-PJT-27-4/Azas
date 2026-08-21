package com.azas.domain.mission.dto;

import com.azas.domain.mission.entity.MissionAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMissionStatusRequest {

    @NotNull
    private MissionAction action;

    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @JsonProperty("destination_account_id")
    private Long destinationAccountId;
}