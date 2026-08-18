package com.azas.domain.child.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PregnancyCharacter {

    BLUEBERRY("블루베리", 7),
    STRAWBERRY("딸기", 9),
    PEACH("복숭아", 13),
    LEMON("레몬", 14),
    AVOCADO("아보카도", 16),
    MANGO("망고", 19),
    BANANA("바나나", 20),
    MELON("멜론", 24),
    EGGPLANT("가지", 28),
    PINEAPPLE("파인애플", 31),
    COCONUT("코코넛", 35),
    WATERMELON("수박", 39);

    private final String displayName;
    private final int startWeek;

    public static PregnancyCharacter currentAt(
            int pregnancyWeek
    ) {
        PregnancyCharacter current = null;

        for (PregnancyCharacter character : values()) {
            if (character.startWeek <= pregnancyWeek) {
                current = character;
            } else {
                break;
            }
        }

        return current;
    }

    public static PregnancyCharacter nextAfter(
            int pregnancyWeek
    ) {
        return Arrays.stream(values())
                .filter(character ->
                        character.startWeek > pregnancyWeek
                )
                .findFirst()
                .orElse(null);
    }
}