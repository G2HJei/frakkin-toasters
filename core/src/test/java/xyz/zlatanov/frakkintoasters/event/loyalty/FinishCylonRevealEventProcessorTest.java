package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class FinishCylonRevealEventProcessorTest extends EventTestHarness<FinishCylonRevealEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(3).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(2).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE).reveal(CYLON_REDUCE_MORALE);
    }

    // "Resurrect" and "Receive Super Crisis".
    @Test
    void shouldResurrectWithASuperCrisisCard() {
        execute(new FinishCylonRevealEvent(2, true));

        assertEquals(RESURRECTION_SHIP, locate(KARA_STARBUCK_THRACE));
        assertEquals(1, player(2).superCrisisCards().size());
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // "Executed Cylons do not get a Super Crisis Card at this time."
    @Test
    void shouldNotGiveASuperCrisisCardAfterAnExecution() {
        execute(new FinishCylonRevealEvent(2, false));

        assertEquals(RESURRECTION_SHIP, locate(KARA_STARBUCK_THRACE));
        assertTrue(player(2).superCrisisCards().isEmpty());
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // "If he was piloting a viper it is returned to the Reserves."
    @Test
    void shouldReturnTheViperOfAPilot() {
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.player(2).pilot(viper.id());

        execute(new FinishCylonRevealEvent(2, false));

        assertEquals(RESURRECTION_SHIP, locate(KARA_STARBUCK_THRACE));
        assertTrue(galacticaBoard.reserves().contains(viper));
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // "If it is the revealing player's turn, his turn then ends."
    @Test
    void shouldEndTheTurnOfTheCurrentPlayer() {
        ops.changeCurrentPlayer(2);

        execute(new FinishCylonRevealEvent(2, true));

        assertTrue(game.turn().endedEarly());
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // A Cylon who reveals with an action another player's card gave him doesn't end that player's turn.
    @Test
    void shouldNotEndTheTurnOfAnotherPlayer() {
        ops.changeCurrentPlayer(1);

        execute(new FinishCylonRevealEvent(2, true));

        assertFalse(game.turn().endedEarly());
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // "The revealing Cylon looks at all of his facedown Loyalty Cards and gives them to one human player of his choice."
    // The titles go first.
    @Test
    void shouldGiveTheOtherLoyaltyCardsToAHuman() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON);

        execute(new FinishCylonRevealEvent(2, true));

        assertFollowup(all(
                single(new GiveTitlesToNextInLineEvent(2)),
                one(new GiveLoyaltyCardsEvent(2, 3), new GiveLoyaltyCardsEvent(2, 1))));
    }

    // "He gives this card to another player no matter how much distance has been traveled."
    @Test
    void shouldPassTheMutineerCard() {
        player(2).loyaltyCards().addOnTop(MUTINEER);

        execute(new FinishCylonRevealEvent(2, false));

        assertFollowup(all(
                single(new GiveTitlesToNextInLineEvent(2)),
                one(new PassMutineerCardEvent(2, 3), new PassMutineerCardEvent(2, 1))));
    }

    @Test
    void shouldRejectAPlayerWhoDidntReveal() {
        assertInvalid(new FinishCylonRevealEvent(1, true));
    }
}
