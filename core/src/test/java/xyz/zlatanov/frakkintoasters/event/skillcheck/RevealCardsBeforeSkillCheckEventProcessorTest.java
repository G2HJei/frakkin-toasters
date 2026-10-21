package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class RevealCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<RevealCardsBeforeSkillCheckEvent> {

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldFollowUpWithDestinyDeckWhenNoCardPlayed() {
        execute(new RevealCardsBeforeSkillCheckEvent());
        assertFollowup(new PlayFromDestinyDeckEvent());
    }

    @Test
    void shouldNotPlayFromDestinyDeckWhenGutsAndInitiativeIsInPlay() {
        game.activeSkillCheck().playedBefore(1, List.of(new SkillCard(0, GUTS_AND_INITIATIVE)));
        execute(new RevealCardsBeforeSkillCheckEvent());
    }

}