package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class AdministrationEventProcessorTest extends EventTestHarness<AdministrationEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(TOM_ZAREK);
        moveTo(ADMINISTRATION, TOM_ZAREK);
    }

    // "Action: Draw 1 Mutiny Card. If the President has any Mutiny Cards, choose a player to gain the President title."
    // The Mutiny Card may be the President's own, so the President's cards count after the draw.
    @Test
    void shouldDrawAMutinyCardThenCheckThePresident() {
        assertTrue(LocationEvent.canAct(game, player(1)));

        execute(new AdministrationEvent(1));

        assertFollowup(all(new DrawMutinyCardEvent(1), new ReplaceMutinousPresidentEvent(1)));
    }

    @Test
    void shouldNotAllowElsewhere() {
        moveTo(COMMAND, TOM_ZAREK);
        assertInvalid(new AdministrationEvent(1));
    }
}
