package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;

class CrashLandingFailEventProcessorTest extends EventTestHarness<CrashLandingFailEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(FELIX_GAETA);
        moveTo(COMMAND, FELIX_GAETA);
    }

    @Test
    void shouldChooseBetweenFuelAndMoraleWithSickbay() {
        execute(new CrashLandingFailEvent());

        assertFollowup(one(
                single(new LoseResourceEvent(FUEL, 1)),
                all(new LoseResourceEvent(MORALE, 1), new SendToSickbayEvent(1))));
    }

    // A revealed Cylon resolving the card at "Caprica" stays there (rule 6.1.2.1).
    @Test
    void shouldNotSendARevealedCylonToSickbay() {
        moveTo(CAPRICA, FELIX_GAETA);
        revealCylon();

        execute(new CrashLandingFailEvent());

        assertFollowup(one(new LoseResourceEvent(FUEL, 1), new LoseResourceEvent(MORALE, 1)));
    }
}
