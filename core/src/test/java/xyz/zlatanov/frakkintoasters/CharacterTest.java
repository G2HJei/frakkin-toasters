package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.skill.SkillSetOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.character.CharacterType.SUPPORT;

class CharacterTest {

    @Test
    void shouldProvideCharacterTypeInfo() {
        assertEquals(SUPPORT, CHIEF_GALEN_TYROL.type());
    }

    @Test
    void shouldProvideSkillSet() {
        assertEquals(SkillSetOption.skillSet("1xTa, 2xPi, 2xL/Po"), LEE_APOLLO_ADAMA.skillSet());
    }
}