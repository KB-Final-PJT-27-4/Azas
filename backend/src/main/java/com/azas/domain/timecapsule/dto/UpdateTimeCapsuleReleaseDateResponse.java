package com.azas.domain.timecapsule.dto;

import com.azas.domain.timecapsule.entity.TimeCapsule;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@ApiModel(description = "타임캡슐 공개일 변경 응답")
public class UpdateTimeCapsuleReleaseDateResponse {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final long timeCapsuleId;
    private final LocalDate releaseDate;
    private final long dDay;
    private final String status;

    private UpdateTimeCapsuleReleaseDateResponse(
            long timeCapsuleId,
            LocalDate releaseDate,
            long dDay,
            String status
    ) {
        this.timeCapsuleId = timeCapsuleId;
        this.releaseDate = releaseDate;
        this.dDay = dDay;
        this.status = status;
    }

    public static UpdateTimeCapsuleReleaseDateResponse of(
            TimeCapsule timeCapsule,
            LocalDate releaseDate
    ) {
        long days = ChronoUnit.DAYS.between(
                LocalDate.now(SERVICE_ZONE),
                releaseDate
        );
        return new UpdateTimeCapsuleReleaseDateResponse(
                timeCapsule.getTimeCapsuleId(),
                releaseDate,
                Math.max(days, 0),
                timeCapsule.getStatus().name()
        );
    }

    @ApiModelProperty(example = "3")
    @JsonProperty("time_capsule_id")
    public long getTimeCapsuleId() {
        return timeCapsuleId;
    }

    @ApiModelProperty(example = "2027-08-08")
    @JsonProperty("release_date")
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @ApiModelProperty(example = "352")
    @JsonProperty("d_day")
    public long getDDay() {
        return dDay;
    }

    @ApiModelProperty(example = "COLLECTING")
    public String getStatus() {
        return status;
    }
}
