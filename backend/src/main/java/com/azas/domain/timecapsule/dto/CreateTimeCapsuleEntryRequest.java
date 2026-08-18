package com.azas.domain.timecapsule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class CreateTimeCapsuleEntryRequest {

    @NotNull
    @Positive
    @JsonProperty("account_transaction_id")
    private Long accountTransactionId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String message;

    public String getTrimmedTitle() {
        return title.trim();
    }

    public String getTrimmedMessage() {
        return message.trim();
    }
}
