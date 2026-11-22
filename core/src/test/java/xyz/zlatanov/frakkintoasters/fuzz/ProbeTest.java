package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind;
import xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESS_ROOM;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;

class ProbeTest {

    // Once a known bug (F-2928df): the move didn't check that the card it discards is in the hand, so it took one
    // William Adama doesn't hold, and the deck's assert failed. Now the probe is rejected and changes nothing.
    @Test
    void aDiscardOfACardNotInTheHandIsRejected() {
        val engine = atAdamasMove();
        val player = askedPlayer(engine);
        val hand = engine.view().player(player).skillCards().cards();
        val notHeld = AllCardsProvider.engineeringCards().cards().stream()
                .filter(card -> !hand.contains(card))
                .findFirst()
                .orElseThrow();

        // Colonial One is another ship, so the move costs a card
        val result = Probe.submit(engine, player, new MoveEvent(player, PRESS_ROOM, notHeld));

        assertFalse(result.taken());
        assertNull(result.finding());
    }

    @Test
    void aRejectedInputThatLeavesTheGameAsItWasIsNoFinding() {
        val engine = atAdamasMove();
        val player = askedPlayer(engine);
        val here = engine.view().locate(engine.view().player(player).character()).orElseThrow();

        val result = Probe.submit(engine, player, new MoveEvent(player, here, null));

        assertFalse(result.taken());
        assertNull(result.finding());
    }

    @Test
    void anInputTheGameTakesGoesOn() {
        val engine = atAdamasMove();
        val player = askedPlayer(engine);

        val result = Probe.submit(engine, player, new MoveEvent(player, RESEARCH_LAB, null));

        assertTrue(result.taken());
        assertNull(result.finding());
        assertEquals(RESEARCH_LAB, engine.view().locate(engine.view().player(player).character()).orElseThrow());
    }

    // The Movement step of turn 1: William Adama is asked where he moves.
    private static GameEngine atAdamasMove() {
        val engine = ScriptedTurns.start();
        ScriptedTurns.inputs().subList(0, 7).forEach(input -> input.apply(engine.view()).applyTo(engine));
        return engine;
    }

    private static int askedPlayer(GameEngine engine) {
        val pending = (AwaitingDecision) engine.pendingInput().orElseThrow();
        assertEquals(MoveEvent.class, pending.decision().action());
        return pending.decision().playerNumber();
    }
}
