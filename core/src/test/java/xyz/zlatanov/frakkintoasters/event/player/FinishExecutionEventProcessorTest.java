package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class FinishExecutionEventProcessorTest extends EventTestHarness<FinishExecutionEvent> {

    // Laura Roslin was executed, and her player chose Saul Tigh.
    @BeforeEach
    void setUpAnExecution() {
        player(2).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        ops.player(2).removeCharacter();
        player(2).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
    }

    // "The player adds one card from the "You Are Not a Cylon" deck to the Loyalty deck, shuffles the Loyalty deck
    // thoroughly, and draws one new Loyalty Card"
    @Test
    void shouldDealANewLoyaltyCard() {
        loyaltyNotCylonDeck.nextCard(NOT_CYLON);

        execute(new FinishExecutionEvent(2, LAURA_ROSLIN));

        assertTrue(loyaltyDeck.wasShuffled());
        assertEquals(List.of(NOT_CYLON), player(2).loyaltyCards().cards());
        assertFollowup(new DetermineNextPresidentEvent());
    }

    // "If the Admiral, President or CAG is executed, the title will change hands after the new character is selected."
    @Test
    void shouldPassEveryTitleTheExecutedCharacterHeld() {
        admiral(LAURA_ROSLIN);
        loyaltyNotCylonDeck.nextCard(NOT_CYLON);

        execute(new FinishExecutionEvent(2, LAURA_ROSLIN));

        assertFollowup(all(new DetermineNextPresidentEvent(), new DetermineNextAdmiralEvent()));
    }

    @Test
    void shouldNotFinishBeforeANewCharacterIsChosen() {
        setUpGame();
        player(2).character(LAURA_ROSLIN);
        ops.player(2).removeCharacter();

        assertInvalid(new FinishExecutionEvent(2, LAURA_ROSLIN));
    }
}
