package xyz.zlatanov.frakkintoasters.engine;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScopeActorTest extends TestHarness {

    @Test
    void aQueuedPlayerEventIsLoggedUnderItsPlayer() {
        ops.actingPlayer(2);
        assertEquals(3, ScopeActor.ofQueued(game, new NoOpEvent(3)));
    }

    @Test
    void anyOtherQueuedEventIsLoggedUnderTheActingPlayer() {
        ops.changeCurrentPlayer(1);
        ops.actingPlayer(2);
        assertEquals(2, ScopeActor.ofQueued(game, new DamageGalacticaEvent()));
    }
}
