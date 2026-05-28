package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActivateHeavyRaidersAndCenturionsAction;
import xyz.zlatanov.frakkintoasters.event.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class CylonFleetActionEventTest {

    Game           game           = Game.builder().build();
    GalacticaBoard galacticaBoard = game.boards().galactica();

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromSingleBasestar() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, game.cylonShips().basestar().orElseThrow());
        executeAction(null);
        assertCylonShips(GALACTICA_SPACE_2_OCLOCK);
    }

    @Test
    void shouldLaunch2RaidersAndHeavyRaiderFromEachBasestar() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, game.cylonShips().basestar().orElseThrow());
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, game.cylonShips().basestar().orElseThrow());
        executeAction(null);
        assertCylonShips(GALACTICA_SPACE_2_OCLOCK, GALACTICA_SPACE_4_OCLOCK);
    }

    @Test
    void shouldFollowUpWithActivateRaidersEvent() {
        val followup = executeAction(RAIDER);
        assertEquals(single(new ActivateRaidersEvent()), followup);
    }

    @Test
    void shouldFollowUpWithActivateHeavyRaidersAndCenturionsEvent() {
        val followup = executeAction(HEAVY_RAIDER);
        assertEquals(single(new ActivateHeavyRaidersAndCenturionsAction()), followup);
    }

    @Test
    void shouldAcceptOnlyValidOrEmptyTypeToActivate() {
        Arrays.stream(ShipType.values()).toList().stream()
                .filter(t -> t != HEAVY_RAIDER && t != RAIDER)
                .forEach(invalidType ->
                        assertFalse(new CylonFleetActionEvent(1, invalidType).isValid(game)));
    }

    Followup executeAction(ShipType shipType) {
        return new CylonFleetActionEvent(1, shipType).execute(game);
    }

    void assertCylonShips(Location... locations) {
        for (val location : locations) {
            assertEquals(2, galacticaBoard.shipsIn(location, Raider.class).size());
            assertEquals(1, galacticaBoard.shipsIn(location, HeavyRaider.class).size());
        }
    }
}