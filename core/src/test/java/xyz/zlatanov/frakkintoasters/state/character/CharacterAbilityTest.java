package xyz.zlatanov.frakkintoasters.state.character;

import org.junit.jupiter.api.Test;

import lombok.val;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.AbilityKind.NEGATIVE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.BROADCAST;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CYLON_DETECTOR;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RECKLESS;

class CharacterAbilityTest {

    // Each sheet prints three: two abilities and the negative one, and one of them is "once per game".
    @Test
    void everyCharacterHasThreeAbilitiesOneNegativeAndOneMiracle() {
        for (val character : Character.values()) {
            val abilities = CharacterAbility.of(character);
            assertEquals(3, abilities.size(), character.name());
            assertEquals(1, abilities.stream().filter(a -> a.kind() == NEGATIVE).count(), character.name());
            assertEquals(1, abilities.stream().filter(CharacterAbility::isMiracle).count(), character.name());
        }
        assertEquals(99, CharacterAbility.values().length);
    }

    @Test
    void broadcastSpendsThreeMiracleTokens() {
        assertEquals(3, BROADCAST.miracleTokens());
        assertEquals(1, CYLON_DETECTOR.miracleTokens());
        assertEquals(0, RECKLESS.miracleTokens());
        assertTrue(Arrays.stream(CharacterAbility.values()).allMatch(a -> a.miracleTokens() <= 3));
    }
}
