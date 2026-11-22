package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class OfferUnmannedViperActivationEventProcessorTest extends EventTestHarness<OfferUnmannedViperActivationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        cag(KARA_STARBUCK_THRACE);
    }

    @Test
    void theCagMayActivateAViperOfTheReserves() {
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new OfferUnmannedViperActivationEvent(1));

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1)));
    }

    // "Review Camera Footage" damaged the vipers of the "Reserves" before it got there (F-78b08a).
    @Test
    void nothingWithoutAViperToActivate() {
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        galacticaBoard.reserves().stream()
                .filter(HumanFighter.class::isInstance)
                .map(Ship::id)
                .toList()
                .forEach(id -> galacticaBoard.removeFromReserves((int) id));

        execute(new OfferUnmannedViperActivationEvent(1));
    }

    // A pilot activates his own viper.
    @Test
    void nothingForACagWhoIsPiloting() {
        viperAt(GALACTICA_SPACE_2_OCLOCK).pilot(KARA_STARBUCK_THRACE);

        execute(new OfferUnmannedViperActivationEvent(1));
    }
}
