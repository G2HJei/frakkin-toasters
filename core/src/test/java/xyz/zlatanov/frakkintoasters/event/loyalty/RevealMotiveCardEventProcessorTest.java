package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.FTL_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.card.MotiveCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

// "A Cylon Leader may reveal a Motive Card any time its requirements are currently being met." (rule 6.2.2.1)
class RevealMotiveCardEventProcessorTest extends EventTestHarness<RevealMotiveCardEvent> {

    @BeforeEach
    void players() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        player(3).character(CAVIL);
        player(3).motiveCards().addOnTop(END_THE_CHASE, KEEP_THEM_DOCILE);
    }

    @Test
    void aCylonLeaderRevealsACardWhoseRequirementIsMet() {
        galacticaBoard.damage(FTL_CONTROL); // "FTL Control" or the "Admiral's Quarters" is damaged

        execute(new RevealMotiveCardEvent(3, END_THE_CHASE));

        assertEquals(List.of(END_THE_CHASE), game.player(3).motiveCards().revealedCards());
        assertEquals(List.of(KEEP_THEM_DOCILE), game.player(3).motiveCards().cards());
    }

    @Test
    void aCardWhoseRequirementIsntMetCantBeRevealed() {
        assertInvalid(new RevealMotiveCardEvent(3, END_THE_CHASE));
    }

    // "Reveal this card if the game is over and food is 4 or less."
    @Test
    void aCardThatWaitsForTheEndCantBeRevealedDuringTheGame() {
        galacticaBoard.decreaseFood(galacticaBoard.food() - 4);

        assertInvalid(new RevealMotiveCardEvent(3, KEEP_THEM_DOCILE));
    }

    @Test
    void aCardHeDoesntHoldCantBeRevealed() {
        galacticaBoard.damage(FTL_CONTROL);

        assertInvalid(new RevealMotiveCardEvent(3, A_JUSTIFIED_RESPONSE));
    }

    @Test
    void anotherPlayerCantRevealHisCard() {
        galacticaBoard.damage(FTL_CONTROL);

        assertInvalid(new RevealMotiveCardEvent(1, END_THE_CHASE));
    }

    @Test
    void aMissingCardIsRejected() {
        assertInvalid(new RevealMotiveCardEvent(3, null));
    }

    @Test
    void aRevealedCardCantBeRevealedAgain() {
        galacticaBoard.damage(FTL_CONTROL);
        player(3).motiveCards().reveal(END_THE_CHASE);

        assertInvalid(new RevealMotiveCardEvent(3, END_THE_CHASE));
    }
}
