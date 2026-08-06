package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

@Getter
@NoArgsConstructor
public class CreateTimeCapsuleExportRequest {

    @NotBlank
    @JsonProperty("export_type")
    private String exportType;

    @Size(max = 10)
    private Map<String, Object> options;
}
