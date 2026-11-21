package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;
import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscardEvent;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.character.PoliticallyAdroitEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.discardExactly;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.KLEPTOMANIA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

class EndOfMovementStepEventProcessorTest extends EventTestHarness<EndOfMovementStepEvent> {

    private static final SkillCard ORDER    = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard ORDER_2  = new SkillCard(2, EXECUTIVE_ORDER);

    // "Politically Adroit" is a choice: a card for 2, or nothing.
    @Test
    void ellenMayGiveACardToAHumanWithHer() {
        player(1).character(ELLEN_TIGH);
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, ELLEN_TIGH);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(1).gainSkillCards(ORDER);

        execute(new EndOfMovementStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, PoliticallyAdroitEvent.class), new NoOpEvent(1)));
    }

    @Test
    void ellenAloneIsAskedNothing() {
        player(1).character(ELLEN_TIGH);
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, ELLEN_TIGH);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(1).gainSkillCards(ORDER);

        execute(new EndOfMovementStepEvent());
    }

    @Test
    void ellenWithoutACardIsAskedNothing() {
        player(1).character(ELLEN_TIGH);
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, ELLEN_TIGH);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);

        execute(new EndOfMovementStepEvent());
    }

    // "Kleptomania": with another player there, he discards 2 Skill Cards.
    @Test
    void romoWithAnotherPlayerDiscardsTwo() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, TOM_ZAREK);
        player(1).gainSkillCards(ORDER, ORDER_2);

        execute(new EndOfMovementStepEvent());

        assertTrue(game.turn().hasUsed(1, KLEPTOMANIA));
        assertFollowup(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, discardExactly(2)));
    }

    // He can't discard 2, so he goes to the "Brig" at the end of his turn.
    @Test
    void romoWithoutTwoCardsGoesToTheBrigLater() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, TOM_ZAREK);
        player(1).gainSkillCards(ORDER);

        execute(new EndOfMovementStepEvent());

        assertEquals(Set.of(1), game.turn().sentToBrigAtEndOfTurn());
        assertEquals(ADMINISTRATION, locate(ROMO_LAMPKIN));
    }

    @Test
    void romoAloneKeepsHisCards() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(PRESS_ROOM, TOM_ZAREK);

        execute(new EndOfMovementStepEvent());

        assertFalse(game.turn().hasUsed(1, KLEPTOMANIA));
        assertEquals(Set.of(), game.turn().sentToBrigAtEndOfTurn());
    }

    @Test
    void nothingOnceTheTurnEndedEarly() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, TOM_ZAREK);
        ops.endTurnEarly();

        execute(new EndOfMovementStepEvent());

        assertEquals(Set.of(), game.turn().sentToBrigAtEndOfTurn());
    }

    // The discard is forced, so Sharon "Athena" Agathon's "For Love" may make it smaller.
    @Test
    void romosDiscardIsOneForLoveCanReduce() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(TOM_ZAREK);
        player(3).character(SHARON_ATHENA_AGATHON);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, TOM_ZAREK);
        player(1).gainSkillCards(ORDER, ORDER_2);

        execute(new EndOfMovementStepEvent());

        assertFollowup(new ForcedDiscardEvent(1, DiscardKind.CHOSEN, 2, false));
    }
}
