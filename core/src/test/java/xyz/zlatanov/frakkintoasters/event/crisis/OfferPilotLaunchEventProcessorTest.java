package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class OfferPilotLaunchEventProcessorTest extends EventTestHarness<OfferPilotLaunchEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
    }

    @Test
    void aPilotOnGalacticaMayLaunchHimself() {
        execute(new OfferPilotLaunchEvent(1));

        assertFollowup(one(new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER), new NoOpEvent(1)));
    }

    // A pilot before him launched the last viper of the "Reserves" (F-a3729e).
    @Test
    void notOnceTheReservesAreEmpty() {
        galacticaBoard.reserves().stream()
                .filter(HumanFighter.class::isInstance)
                .map(Ship::id)
                .toList()
                .forEach(id -> galacticaBoard.removeFromReserves((int) id));

        execute(new OfferPilotLaunchEvent(1));
    }
}
