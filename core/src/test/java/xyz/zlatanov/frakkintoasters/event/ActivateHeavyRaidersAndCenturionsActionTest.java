package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.POSITION_1;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

class ActivateHeavyRaidersAndCenturionsActionTest extends EventTest {

    HeavyRaider heavyRaider;
    Centurion   centurion;

    @BeforeEach
    void setUp() {
        game.setupGalacticaBoard();
        heavyRaider = heavyRaider();
        centurion = centurion();
    }

    @Test
    void shouldMoveHeavyRaiderTowardsNearestLaunchIcon() {
        placeHeavyRaider(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        executeAction();
        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(heavyRaider));
    }

    @Test
    void shouldAdvanceCenturionsOnBoardingPartyTrack() {
        boardGalactica(centurion);
        executeAction();
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldSpawnNewHeavyRaiderIfNoneOnBoard() {
        executeAction();
        assertEquals(1, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, HeavyRaider.class).size());
    }

    @Test
    void shouldMoveBothHeavyRaidersAndCenturions() {
        placeHeavyRaider(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        boardGalactica(centurion);

        executeAction();

        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(heavyRaider));
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldConvertHeavyRaiderAtLaunchIconIntoCenturion() {
        placeHeavyRaider(GALACTICA_SPACE_4_OCLOCK, heavyRaider);
        val centurionsAvailable = cylonShips.centurions().size();

        executeAction();

        assertNull(galacticaBoard.locate(heavyRaider));
        val track = galacticaBoard.boardingPartyTrack();
        assertEquals(1, track.size());
        assertEquals(START, track.values().iterator().next());
        assertEquals(centurionsAvailable - 1, cylonShips.centurions().size());
    }

    @Test
    void shouldKeepHeavyRaidersIfAlLCenturionsAreOnBoards() {
        while (!cylonShips.centurions().isEmpty()) {
            boardGalactica(cylonShips.centurion().orElseThrow());
        }
        placeHeavyRaider(GALACTICA_SPACE_6_OCLOCK, heavyRaider);

        executeAction();

        assertEquals(GALACTICA_SPACE_6_OCLOCK, galacticaBoard.locate(heavyRaider));
    }

    @Test
    void shouldFollowWithPlaceHeavyRaiderOnCylonFleetBoardAndAdvancePursuitTrack() {
        setUpGame(Game.builder().build());
        val followup = executeAction();
        assertEquals(all(
                        new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                        new AdvancePursuitTrackEvent()),
                followup);
    }

    @Test
    void shouldEndGameIfBoardingPartyReachEnd() {
        galacticaBoard.boardGalactica(centurion)
                .advanceBoardingParty()
                .advanceBoardingParty()
                .advanceBoardingParty();

        val followup = executeAction();

        assertEquals(one(new CylonsWinEvent()), followup);
    }

    void placeHeavyRaider(Location location, HeavyRaider heavyRaider) {
        galacticaBoard.place(location, heavyRaider);
    }

    void boardGalactica(Centurion centurion) {
        galacticaBoard.boardGalactica(centurion);
    }

    Followup executeAction() {
        return execute(new ActivateHeavyRaidersAndCenturionsAction());
    }
}
