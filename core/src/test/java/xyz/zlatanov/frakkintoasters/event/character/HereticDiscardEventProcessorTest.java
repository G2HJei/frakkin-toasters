package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HERETIC;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FLEET_MOBILIZATION;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;

class HereticDiscardEventProcessorTest extends EventTestHarness<HereticDiscardEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(DANNA_BIERS);
        moveTo(CAPRICA, DANNA_BIERS);
        player(1).superCrisisCards().addOnTop(INBOUND_NUKES);
    }

    // The card goes to the Super Crisis discard pile, face up.
    @Test
    void discardsTheSuperCrisisCard() {
        execute(new HereticDiscardEvent(1, INBOUND_NUKES));

        assertTrue(player(1).superCrisisCards().isEmpty());
        assertEquals(List.of(INBOUND_NUKES), game.decks().superCrisis().discardedCards());
        assertTrue(game.turn().hasUsed(1, HERETIC));
    }

    @Test
    void notACardSheDoesntHold() {
        assertInvalid(new HereticDiscardEvent(1, FLEET_MOBILIZATION));
    }
}
