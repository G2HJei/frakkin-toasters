package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
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

    @Test
    void shouldTriggerTieBreakWhenMultipleCardsOfSameTypeInPlay() {
        game.activeSkillCheck()
                .playedBefore(1, List.of(new SkillCard(1, AT_ANY_COST)))
                .playedBefore(2, List.of(new SkillCard(2, AT_ANY_COST)));
        execute(new RevealCardsBeforeSkillCheckEvent());
        assertFollowup(
                all(
                        new PlayerDecisionEvent<>(1, TiebreakCardsBeforeSkillCheckEvent.class),
                        new PlayFromDestinyDeckEvent()));
    }

}