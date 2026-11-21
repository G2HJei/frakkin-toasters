package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CHIEF_GALEN_TYROL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;

class BuildNukeEventProcessorTest extends EventTestHarness<BuildNukeEvent> {

    private static final SkillCard BUILD_NUKE_6 = new SkillCard(6, BUILD_NUKE);

    @BeforeEach
    void setUp() {
        player(1).character(CHIEF_GALEN_TYROL);
        player(1).gainSkillCards(BUILD_NUKE_6);
    }

    // The Admiral starts with 2 nuke tokens.
    @Test
    void theAdmiralGainsANuke() {
        execute(new BuildNukeEvent(1, BUILD_NUKE_6));

        assertEquals(3, game.nukes());
        assertNoSkillCards(1);
        assertEquals(List.of(BUILD_NUKE_6), engineeringDeck.discardedCards());
    }

    // The Admiral may not hold more than 3 (rule 11.6.1).
    @Test
    void notWithThreeNukes() {
        ops.gainNuke();

        assertInvalid(new BuildNukeEvent(1, BUILD_NUKE_6));
    }
}
