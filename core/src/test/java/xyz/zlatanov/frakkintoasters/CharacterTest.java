package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.CharacterType.SUPPORT;

class CharacterTest {

    @Test
    void shouldProvideCharacterTypeInfo() {
        assertEquals(SUPPORT, CHIEF_GALEN_TYROL.type());
    }

    @Test
    void shouldProvideSkillSet() {
        assertEquals(SkillSetOption.skills("1xTa, 2xPi, 2xL/Po"), LEE_APOLLO_ADAMA.skillSet());
    }
}