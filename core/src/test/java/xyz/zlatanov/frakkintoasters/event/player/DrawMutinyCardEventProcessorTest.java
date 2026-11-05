package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class DrawMutinyCardEventProcessorTest extends EventTestHarness<DrawMutinyCardEvent> {

    @Test
    void shouldDrawMutinyCard() {
        player(1).character(LAURA_ROSLIN);
        mutinyDeck.nextCard(PANIC);

        execute(new DrawMutinyCardEvent(1));

        assertEquals(PANIC, player(1).mutinyCards().cards().getFirst());
        assertEquals(1, player(1).mutinyCards().size());
    }

    @Test
    void shouldNotDrawForRevealedCylon() {
        player(1).character(LAURA_ROSLIN);
        revealCylon();

        execute(new DrawMutinyCardEvent(1));

        assertTrue(player(1).mutinyCards().isEmpty());
    }

    @Test
    void shouldNotDrawForCylonLeaderWhoIsNotInfiltrating() {
        player(1).character(CAPRICA_SIX);

        execute(new DrawMutinyCardEvent(1));

        assertTrue(player(1).mutinyCards().isEmpty());
    }

    @Test
    void shouldDrawForInfiltratingCylonLeader() {
        player(1).character(CAPRICA_SIX).infiltrateGalactica();

        execute(new DrawMutinyCardEvent(1));

        assertEquals(1, player(1).mutinyCards().size());
    }
}
