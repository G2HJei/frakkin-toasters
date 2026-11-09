package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
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
                        single(
                                new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER)),
                        one(
                                new ActivateViperEvent(1),
                                new NoOpEvent(1))));
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
                        single(
                                new PlayerDecisionEvent<>(1, LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER)),
                        one(
                                new ActivateViperEvent(1),
                                new NoOpEvent(1))));
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
}
