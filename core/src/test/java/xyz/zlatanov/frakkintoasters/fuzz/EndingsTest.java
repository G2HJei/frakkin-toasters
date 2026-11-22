package xyz.zlatanov.frakkintoasters.fuzz;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.fuzz.Endings.Ending.*;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.Team.HUMANS;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.*;

class EndingsTest extends TestHarness {

    @Test
    void aGameThatGoesOnHasNoEnding() {
        assertEquals(Optional.empty(), Endings.classify(game));
    }

    @Test
    void aResourceAt0() {
        ops.galactica().decreaseResource(Resource.FOOD, galacticaBoard.food());
        ops.endGame(CYLONS, List.of());

        assertEquals(Optional.of(FOOD_AT_0), Endings.classify(game));
    }

    @Test
    void sixDamagedGalacticaLocations() {
        LocationsArea.GALACTICA.locations().stream().limit(6).forEach(galacticaBoard::damage);
        ops.endGame(CYLONS, List.of());

        assertEquals(Optional.of(GALACTICA_DESTROYED), Endings.classify(game));
    }

    @Test
    void aCenturionAtTheEndOfTheBoardingPartyTrack() {
        ops.galactica().placeCenturion();
        for (int step = 0; step < 4; step++) {
            ops.galactica().advanceBoardingParty();
        }
        ops.endGame(CYLONS, List.of());

        assertEquals(Optional.of(BOARDED), Endings.classify(game));
    }

    @Test
    void theHumansReachKobol() {
        reachKobol();
        ops.endGame(HUMANS, List.of(1, 2, 3));

        assertEquals(Optional.of(KOBOL_HUMANS), Endings.classify(game));
    }

    @Test
    void aPersonalGoalEmptiesAResourceAtKobol() {
        reachKobol();
        ops.galactica().decreaseResource(Resource.MORALE, galacticaBoard.morale());
        ops.endGame(CYLONS, List.of());

        assertEquals(Optional.of(KOBOL_CYLONS), Endings.classify(game));
    }

    @Test
    void aWinThatNoEndingExplains() {
        ops.endGame(CYLONS, List.of());

        assertEquals(Optional.empty(), Endings.classify(game));
    }

    @Test
    void aHumanWinBeforeKobol() {
        ops.endGame(HUMANS, List.of(1, 2, 3));

        assertEquals(Optional.empty(), Endings.classify(game));
    }

    private void reachKobol() {
        ops.galactica().reachDestination(ASTEROID_FIELD);
        ops.galactica().reachDestination(CYLON_AMBUSH);
        ops.galactica().reachDestination(BARREN_PLANET);
    }
}
