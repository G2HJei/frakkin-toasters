package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class RevealCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<RevealCardsBeforeSkillCheckEvent> {


    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldTriggerTieBreakWhenMultipleCardsOfSameTypeInPlay() {
        addPlayedBefore(1);
        addPlayedBefore(2);

        execute(new RevealCardsBeforeSkillCheckEvent());

        assertFollowup(
                all(
                        new PlayerDecisionEvent<>(1, TiebreakCardsBeforeSkillCheckEvent.class),
                        new PlayFromDestinyDeckEvent()));
    }

    //todo test cards are revealed

    private void addPlayedBefore(int playerNumber) {
        game.activeSkillCheck().playedBefore(playerNumber, List.of(new SkillCard(0, AT_ANY_COST)));
    }

}