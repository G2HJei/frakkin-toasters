package xyz.zlatanov.frakkintoasters.state.character;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.SUPPORT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.LEADERSHIP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

class CharacterTest {

    @Test
    void shouldProvideCharacterTypeInfo() {
        assertEquals(SUPPORT, CHIEF_GALEN_TYROL.type());
    }

    @Test
    void shouldProvideSkillSet() {
        assertEquals(SkillSetOption.skillSet("1xTa, 2xPi, 2xL/Po"), LEE_APOLLO_ADAMA.skillSet());
    }

    @Test
    void shouldKeepThePrintedOrderOfTheSkillSet() {
        val options = List.copyOf(FELIX_GAETA.skillSet());

        assertEquals(List.of(2, 1, 2), options.stream().map(SkillSetOption::count).toList());
        assertEquals(List.of(TACTICS), List.copyOf(options.get(0).colors()));
        assertEquals(List.of(ENGINEERING), List.copyOf(options.get(1).colors()));
        assertEquals(List.of(LEADERSHIP, POLITICS), List.copyOf(options.get(2).colors()));
    }
}