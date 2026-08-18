package com.azas.domain.child.dto;

import com.azas.domain.child.entity.PregnancyCharacter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PregnancyCharacterResponse {

    private final String code;

    private final String name;

    @JsonProperty("start_week")
    private final int startWeek;

    public static PregnancyCharacterResponse from(
            PregnancyCharacter character
    ) {
        if (character == null) {
            return null;
        }

        return new PregnancyCharacterResponse(
                character.name(),
                character.getDisplayName(),
                character.getStartWeek()
        );
    }
}