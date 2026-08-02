package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class OAuthLoginRequest {

    @NotBlank
    @JsonProperty("authorization_code")
    private String authorizationCode;

    @NotBlank
    @JsonProperty("redirect_uri")
    private String redirectUri;
}
