package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscardNukeTokenEventProcessorTest extends EventTestHarness<DiscardNukeTokenEvent> {

    @Test
    void shouldDiscardANukeToken() {
        execute(new DiscardNukeTokenEvent());

        assertEquals(1, game.nukes());
        assertTrue(journal.facts().contains(new GameFact.NukeDiscarded()));
    }

    // "If you do not have any nuke tokens, you cannot choose this option."
    @Test
    void shouldNotDiscardWithoutNukeTokens() {
        game.discardNuke();
        game.discardNuke();

        assertInvalid(new DiscardNukeTokenEvent());
    }
}
