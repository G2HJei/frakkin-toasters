package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_10_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class HangarDeckEventProcessorTest extends EventTestHarness<HangarDeckEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldLaunchViperWithExtraAction() {
        execute(new HangarDeckEvent(1));
        assertFollowup(
                all(
                        new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER),
                        new GrantEvent(1, ACTION, GrantEvent.GrantedBy.HANGAR_DECK)));
    }

    @Test
    void shouldNotAllowNonPilotToUseLocation() {
        player(2).character(LOUIS_HOSHI);
        moveTo(HANGAR_DECK, LOUIS_HOSHI);
        assertInvalid(new HangarDeckEvent(2));
    }

    @Test
    void shouldTakeAnUnmannedViperFromSpaceWhenTheReservesAreEmpty() {
        moveAllHumanFightersFromReservesToSpace();

        execute(new HangarDeckEvent(1));

        assertFollowup(
                all(
                        new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER),
                        new GrantEvent(1, ACTION, GrantEvent.GrantedBy.HANGAR_DECK)));
    }

    @Test
    void shouldNotBeUsedWithoutAViperToLaunch() {
        moveAllHumanFightersFromReservesToSpace()
                .forEach(fighter -> fighter.pilot(LEE_APOLLO_ADAMA));
        assertInvalid(new HangarDeckEvent(1));
    }

    private List<HumanFighter> moveAllHumanFightersFromReservesToSpace() {
        List<HumanFighter> fighters = galacticaBoard.reserves().stream()
                .filter(HumanFighter.class::isInstance)
                .map(HumanFighter.class::cast)
                .toList();
        fighters.forEach(fighter -> {
            galacticaBoard.removeFromReserves(fighter);
            place(GALACTICA_SPACE_10_OCLOCK, fighter);
        });
        return fighters;
    }

    // Not from the "Brig", e.g. through "Resolute" (errata A2.1.1.1).
    @Test
    void shouldNotBeUsedFromTheBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        ops.allowRemoteAction(1, HANGAR_DECK); // as "Resolute" would

        assertFalse(HangarDeckEventProcessor.canAct(game, player(1)));
        assertInvalid(new HangarDeckEvent(1));
    }
}
