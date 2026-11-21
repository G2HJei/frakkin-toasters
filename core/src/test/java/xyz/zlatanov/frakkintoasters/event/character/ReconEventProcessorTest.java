package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RECON;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;

class ReconEventProcessorTest extends EventTestHarness<ReconEvent> {

    @Test
    void looksAtTheTopCrisisCard() {
        player(1).character(SHARON_BOOMER_VALERII);
        moveTo(ARMORY, SHARON_BOOMER_VALERII);
        // at the end of her own turn she may look, or not
        assertEquals(one(new ReconEvent(1), new NoOpEvent(1)), ReconEventProcessor.offer(game));

        execute(new ReconEvent(1));

        assertTrue(game.turn().hasUsed(1, RECON));
        assertFollowup(new LookAtTopCardEvent(1, CRISIS));
    }

    @Test
    void notOfferedToOthers() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_BOOMER_VALERII);

        assertEquals(NONE, ReconEventProcessor.offer(game));
        assertInvalid(new ReconEvent(1));
    }

    @Test
    void notWithAnEmptyCrisisDeck() {
        player(1).character(SHARON_BOOMER_VALERII);
        crisisDeck.clear();

        assertEquals(NONE, ReconEventProcessor.offer(game));
        assertInvalid(new ReconEvent(1));
    }
}
