package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.skillcheck.TiebreakCardsBeforeSkillCheckEvent.PlayedCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class TiebreakCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<TiebreakCardsBeforeSkillCheckEvent> {
    SkillCard investigativeCommittee = new SkillCard(3, INVESTIGATIVE_COMMITTEE);
    SkillCard aSecondChance          = new SkillCard(5, A_SECOND_CHANCE);
    SkillCard restoreOrder           = new SkillCard(3, RESTORE_ORDER);
    SkillCard juryRigged             = new SkillCard(1, JURY_RIGGED);
    SkillCard supportThePeople       = new SkillCard(1, SUPPORT_THE_PEOPLE);

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldReturnLosingCardsOfSameType() {
        playedBefore(1, investigativeCommittee);
        playedBefore(2, investigativeCommittee);

        execute(new TiebreakCardsBeforeSkillCheckEvent(kept(2, investigativeCommittee)));

        assertSkillCards(1, investigativeCommittee);
        assertNoSkillCards(2);
        assertPlayedBefore(1);
        assertPlayedBefore(2, investigativeCommittee);
    }

    @Test
    void shouldReturnLosingRecklessAndRestoreOrderCards() {
        playedBefore(1, restoreOrder);
        playedBefore(2, juryRigged);
        playedBefore(3, supportThePeople);

        execute(new TiebreakCardsBeforeSkillCheckEvent(kept(2, juryRigged)));

        assertSkillCards(1, restoreOrder);
        assertNoSkillCards(2);
        assertSkillCards(3, supportThePeople);
        assertPlayedBefore(2, juryRigged);
    }

    @Test
    void shouldResolveEveryConflictGroup() {
        playedBefore(1, aSecondChance, juryRigged);
        playedBefore(2, aSecondChance, restoreOrder);
        playedBefore(3, investigativeCommittee);

        execute(new TiebreakCardsBeforeSkillCheckEvent(
                kept(1, aSecondChance),
                kept(2, restoreOrder)));

        assertSkillCards(1, juryRigged);
        assertSkillCards(2, aSecondChance);
        assertNoSkillCards(3);
        assertPlayedBefore(1, aSecondChance);
        assertPlayedBefore(2, restoreOrder);
        assertPlayedBefore(3, investigativeCommittee);
    }

    @Test
    void shouldNotAllowKeepingCardWithoutConflict() {
        playedBefore(1, investigativeCommittee);
        playedBefore(2, investigativeCommittee);
        playedBefore(3, restoreOrder);

        assertInvalid(new TiebreakCardsBeforeSkillCheckEvent(
                kept(1, investigativeCommittee),
                kept(3, restoreOrder)));
    }

    @Test
    void shouldNotAllowConflictGroupWithoutKeptCard() {
        playedBefore(1, investigativeCommittee);
        playedBefore(2, investigativeCommittee);

        assertInvalid(new TiebreakCardsBeforeSkillCheckEvent());
    }

    @Test
    void shouldNotAllowMultipleKeptCardsInConflictGroup() {
        playedBefore(1, juryRigged);
        playedBefore(2, supportThePeople);

        assertInvalid(new TiebreakCardsBeforeSkillCheckEvent(
                kept(1, juryRigged),
                kept(2, supportThePeople)));
    }

    private void playedBefore(int playerNumber, SkillCard... cards) {
        game.activeSkillCheck().playedBefore(playerNumber, List.of(cards));
    }

    private PlayedCard kept(int playerNumber, SkillCard card) {
        return new PlayedCard(playerNumber, card);
    }

    private void assertPlayedBefore(int playerNumber, SkillCard... expected) {
        assertEquals(List.of(expected), game.activeSkillCheck().playedBeforeSkillCheck(playerNumber));
    }
}
