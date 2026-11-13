package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class RevealCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<RevealCardsBeforeSkillCheckEvent> {


    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldTriggerTieBreakWhenMultipleCardsOfSameTypeInPlay() {
        addPlayedBefore(1, AT_ANY_COST);
        addPlayedBefore(2, AT_ANY_COST);

        execute(new RevealCardsBeforeSkillCheckEvent());

        assertTieBreak();
    }

    @Test
    void shouldTriggerTieBreakWhenMultipleRecklessCardsInPlay() {
        addPlayedBefore(1, JURY_RIGGED);
        addPlayedBefore(2, SUPPORT_THE_PEOPLE);

        execute(new RevealCardsBeforeSkillCheckEvent());

        assertTieBreak();
    }

    @Test
    void shouldTriggerTieBreakWhenRecklessAndRestoreOrderInPlay() {
        addPlayedBefore(1, RESTORE_ORDER);
        addPlayedBefore(2, AT_ANY_COST);

        execute(new RevealCardsBeforeSkillCheckEvent());

        assertTieBreak();
    }

    @Test
    void shouldNotTriggerTieBreakWhenSingleRecklessOrRestoreOrderInPlay() {
        addPlayedBefore(1, RESTORE_ORDER);
        addPlayedBefore(2, INVESTIGATIVE_COMMITTEE);

        execute(new RevealCardsBeforeSkillCheckEvent());

        assertFollowup(new ResolveCardsBeforeSkillCheckEvent());
    }

    @Test
    void shouldRevealCardsBeforeSkillCheck() {
        execute(new RevealCardsBeforeSkillCheckEvent());
        assertTrue(game.activeSkillCheck().playedBeforeRevealed());
        assertFollowup(new ResolveCardsBeforeSkillCheckEvent());
    }

    private void addPlayedBefore(int playerNumber, SkillCardType skillCardType) {
        game.activeSkillCheck().playedBefore(playerNumber, List.of(new SkillCard(0, skillCardType)));
    }

    private void assertTieBreak() {
        assertFollowup(all(new PlayerDecisionEvent<>(1, TiebreakCardsBeforeSkillCheckEvent.class),
                new ResolveCardsBeforeSkillCheckEvent()));
    }

}
