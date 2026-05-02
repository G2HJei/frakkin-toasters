package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.POSITION_1;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

class ActivateHeavyRaidersAndCenturionsActionTest {

    Game        game        = Game.builder().build().setupGalacticaBoard();
    HeavyRaider heavyRaider = game.cylonShips().heavyRaider();
    Centurion   centurion   = game.cylonShips().centurion();

    @Test
    void shouldMoveHeavyRaiderTowardsNearestLaunchIcon() {
        placeHeavyRaider(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        executeAction(game);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, game.boards().galactica().locate(heavyRaider));
    }

    @Test
    void shouldAdvanceCenturionsOnBoardingPartyTrack() {
        boardGalactica(centurion);
        executeAction(game);
        assertEquals(POSITION_1, game.boards().galactica().boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldSpawnNewHeavyRaiderIfNoneOnBoard() {
        executeAction(game);
        assertEquals(1, game.boards().galactica().shipsIn(GALACTICA_SPACE_8_OCLOCK, HEAVY_RAIDER).size());
    }

    @Test
    void shouldMoveBothHeavyRaidersAndCenturions() {
        placeHeavyRaider(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        boardGalactica(centurion);

        executeAction(game);

        assertEquals(GALACTICA_SPACE_4_OCLOCK, game.boards().galactica().locate(heavyRaider));
        assertEquals(POSITION_1, game.boards().galactica().boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldConvertHeavyRaiderAtLaunchIconIntoCenturion() {
        placeHeavyRaider(GALACTICA_SPACE_4_OCLOCK, heavyRaider);
        val centurionsAvailable = game.cylonShips().centurions().size();

        executeAction(game);

        assertNull(game.boards().galactica().locate(heavyRaider));
        val track = game.boards().galactica().boardingPartyTrack();
        assertEquals(1, track.size());
        assertEquals(START, track.values().iterator().next());
        assertEquals(centurionsAvailable - 1, game.cylonShips().centurions().size());
    }

    @Test
    void shouldKeepHeavyRaidersIfAlLCenturionsAreOnBoards() {
        while (!game.cylonShips().centurions().isEmpty()) {
            boardGalactica(game.cylonShips().centurion());
        }
        placeHeavyRaider(GALACTICA_SPACE_6_OCLOCK, heavyRaider);

        executeAction(game);

        assertEquals(GALACTICA_SPACE_6_OCLOCK, game.boards().galactica().locate(heavyRaider));
    }

    @Test
    void shouldFollowWithPlaceHeavyRaiderOnCylonFleetBoardAndAdvancePursuitTrack() {
        val followup = executeAction(Game.builder().build());
        assertEquals(all(
                        new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                        new AdvancePursuitTrackEvent()),
                followup);
    }

    void placeHeavyRaider(Location location, HeavyRaider heavyRaider) {
        game.boards().galactica().place(location, heavyRaider);
    }

    void boardGalactica(Centurion centurion) {
        game.boards().galactica().boardGalactica(centurion);
    }

    Followup executeAction(Game game) {
        return new ActivateHeavyRaidersAndCenturionsAction().execute(game);
    }
}
