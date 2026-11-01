package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.*;

class SkillCheckTypeTest {

    @ParameterizedTest
    @EnumSource(SkillCheckType.class)
    void shouldHavePrintedDifficultyAndSkillTypes(SkillCheckType type) {
        assertTrue(type.difficulty() > 0);
        assertFalse(type.skillCheckColors().isEmpty());
        assertEquals(type.skillCheckColors().size(), Set.copyOf(type.skillCheckColors()).size());
    }

    @ParameterizedTest
    @EnumSource(SkillCheckType.class)
    void shouldHavePartialPassBelowDifficulty(SkillCheckType type) {
        if (type.partialPass() != null) {
            assertTrue(type.partialPass() > 0 && type.partialPass() < type.difficulty());
        }
    }

    @Test
    void shouldBeTriggeredByLocationOnlyForGameBoardLocations() {
        val triggeredByLocation = Arrays.stream(SkillCheckType.values())
                .filter(SkillCheckType::triggeredByLocation)
                .collect(toSet());

        assertEquals(Set.of(ADMINISTRATION, ADMIRALS_QUARTERS, AIRLOCK, BRIG), triggeredByLocation);
    }
}
