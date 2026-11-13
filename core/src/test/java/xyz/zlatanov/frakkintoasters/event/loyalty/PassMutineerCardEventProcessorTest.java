package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class PassMutineerCardEventProcessorTest extends EventTestHarness<PassMutineerCardEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(GAIUS_BALTAR);
        player(3).character(SAUL_TIGH);
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, MUTINEER).reveal(CYLON_DAMAGE_GALACTICA);
    }

    // "That player must then draw one Mutiny Card and gives each Title Card he has to the player whose character is
    // highest in the order of succession for that title (excluding himself)."
    @Test
    void shouldMakeTheHumanTheMutineer() {
        execute(new PassMutineerCardEvent(1, 2));

        assertTrue(player(2).isMutineer());
        assertFalse(player(1).isMutineer());
        assertEquals(List.of(MUTINEER), player(2).loyaltyCards().cards());
        assertFollowup(all(new DrawMutinyCardEvent(2), new GiveTitlesToNextInLineEvent(2)));
    }

    @Test
    void shouldNotPassACardHeDoesNotHave() {
        assertInvalid(new PassMutineerCardEvent(2, 3));
    }

    @Test
    void shouldNotPassToARevealedCylon() {
        player(3).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);
        assertInvalid(new PassMutineerCardEvent(1, 3));
    }
}
