package com.azas.domain.child.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PregnancyStatusResponse {

    @JsonProperty("child_id")
    private final Long childId;

    @JsonProperty("child_name")
    private final String childName;

    @JsonProperty("expected_birth_date")
    private final LocalDate expectedBirthDate;

    @JsonProperty("as_of_date")
    private final LocalDate asOfDate;

    @JsonProperty("pregnancy_week")
    private final int pregnancyWeek;

    @JsonProperty("pregnancy_day")
    private final int pregnancyDay;

    @JsonProperty("days_until_birth")
    private final long daysUntilBirth;

    private final PregnancyCharacterResponse character;

    @JsonProperty("next_character")
    private final PregnancyCharacterResponse nextCharacter;

    @JsonProperty("days_until_next_character")
    private final Long daysUntilNextCharacter;
}