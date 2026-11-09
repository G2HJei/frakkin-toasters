package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class MakePresidentEventProcessorTest extends EventTestHarness<MakePresidentEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(LAURA_ROSLIN);
        player(2).character(GAIUS_BALTAR);
        president(LAURA_ROSLIN);
    }

    @Test
    void shouldChangeThePresident() {
        execute(new MakePresidentEvent(GAIUS_BALTAR));
        assertEquals(GAIUS_BALTAR, president());
    }

    @Test
    void shouldGiveTheTitleToAnotherPlayer() {
        assertInvalid(new MakePresidentEvent(LAURA_ROSLIN));
    }

    @Test
    void shouldGiveTheTitleToACharacterInTheGame() {
        assertInvalid(new MakePresidentEvent(TOM_ZAREK));
    }

    @Test
    void shouldNotGiveTheTitleToARevealedCylon() {
        player(3).character(TOM_ZAREK)
                .loyaltyCards()
                .addOnTop(CYLON_SEND_TO_BRIG)
                .reveal(CYLON_SEND_TO_BRIG);
        assertInvalid(new MakePresidentEvent(TOM_ZAREK));
    }

    @Test
    void shouldNotGiveTheTitleToAnInfiltratingCylonLeader() {
        player(4).character(CAPRICA_SIX).infiltrateGalactica();
        assertInvalid(new MakePresidentEvent(CAPRICA_SIX));
    }
}
