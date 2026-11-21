package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CylonPowerSendToBrigEventProcessorTest extends EventTestHarness<CylonPowerSendToBrigEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void shouldSendACharacterOnGalacticaToTheBrig() {
        execute(new CylonPowerSendToBrigEvent(1, 2));

        assertFollowup(new SendToBrigEvent(2));
    }

    // Only a character on Galactica, not on Colonial One.
    @Test
    void shouldRejectACharacterOnColonialOne() {
        assertInvalid(new CylonPowerSendToBrigEvent(1, 3));
    }

    @Test
    void shouldRejectACharacterAlreadyInTheBrig() {
        moveTo(BRIG, WILLIAM_ADAMA);

        assertInvalid(new CylonPowerSendToBrigEvent(1, 2));
    }
}
