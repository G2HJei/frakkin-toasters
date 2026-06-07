package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.POSITION_1;
import static xyz.zlatanov.frakkintoasters.state.track.BoardingParty.START;

class ActivateHeavyRaidersAndCenturionsEventProcessorTest extends EventTestHarness<ActivateHeavyRaidersAndCenturionsEvent> {

    HeavyRaider                            heavyRaider;
    Centurion                              centurion;
    ActivateHeavyRaidersAndCenturionsEvent event = new ActivateHeavyRaidersAndCenturionsEvent();

    @BeforeEach
    void setUp() {
        game.setupGalacticaBoard();
        heavyRaider = heavyRaider();
        centurion = centurion();
    }

    @Test
    void shouldMoveHeavyRaiderTowardsNearestLaunchIcon() {
        place(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        execute(event);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(heavyRaider));
    }

    @Test
    void shouldAdvanceCenturionsOnBoardingPartyTrack() {
        boardGalactica(centurion);
        execute(event);
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldSpawnNewHeavyRaiderIfNoneOnBoard() {
        execute(event);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, HeavyRaider.class, 1);
    }

    @Test
    void shouldMoveBothHeavyRaidersAndCenturions() {
        place(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        boardGalactica(centurion);

        execute(event);

        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(heavyRaider));
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldConvertHeavyRaiderAtLaunchIconIntoCenturion() {
        place(GALACTICA_SPACE_4_OCLOCK, heavyRaider);
        val centurionsAvailable = cylonShips.centurions().size();

        execute(event);

        assertTrue(cylonShips.heavyRaiders().contains(heavyRaider));
        val track = galacticaBoard.boardingPartyTrack();
        assertEquals(1, track.size());
        assertEquals(START, track.values().iterator().next());
        assertEquals(centurionsAvailable - 1, cylonShips.centurions().size());
    }

    @Test
    void shouldKeepHeavyRaidersIfAlLCenturionsAreOnBoards() {
        while (!cylonShips.centurions().isEmpty()) {
            boardGalactica(centurion());
        }
        place(GALACTICA_SPACE_6_OCLOCK, heavyRaider);

        execute(event);

        assertEquals(GALACTICA_SPACE_6_OCLOCK, locate(heavyRaider));
    }

    @Test
    void shouldFollowWithPlaceHeavyRaiderOnCylonFleetBoardAndAdvancePursuitTrack() {
        setUpGame();
        execute(new ActivateHeavyRaidersAndCenturionsEvent());
        assertFollowup(
                Followup.all(
                        new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                        new AdvancePursuitTrackEvent()));
    }

    @Test
    void shouldEndGameIfBoardingPartyReachEnd() {
        galacticaBoard.boardGalactica(centurion)
                .advanceBoardingParty()
                .advanceBoardingParty()
                .advanceBoardingParty();
        execute(new ActivateHeavyRaidersAndCenturionsEvent());
        assertFollowup(one(new CylonsWinEvent()));
    }

    void boardGalactica(Centurion centurion) {
        galacticaBoard.boardGalactica(centurion);
    }
}
