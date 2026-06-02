package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
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
        executeAndAssertNoFollowup(event);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(heavyRaider));
    }

    @Test
    void shouldAdvanceCenturionsOnBoardingPartyTrack() {
        boardGalactica(centurion);
        executeAndAssertNoFollowup(event);
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldSpawnNewHeavyRaiderIfNoneOnBoard() {
        executeAndAssertNoFollowup(event);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, HeavyRaider.class, 1);
    }

    @Test
    void shouldMoveBothHeavyRaidersAndCenturions() {
        place(GALACTICA_SPACE_2_OCLOCK, heavyRaider);
        boardGalactica(centurion);

        executeAndAssertNoFollowup(event);

        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(heavyRaider));
        assertEquals(POSITION_1, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    @Test
    void shouldConvertHeavyRaiderAtLaunchIconIntoCenturion() {
        place(GALACTICA_SPACE_4_OCLOCK, heavyRaider);
        val centurionsAvailable = cylonShips.centurions().size();

        executeAndAssertNoFollowup(event);

        assertNull(galacticaBoard.locate(heavyRaider));
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

        executeAndAssertNoFollowup(event);

        assertEquals(GALACTICA_SPACE_6_OCLOCK, galacticaBoard.locate(heavyRaider));
    }

    @Test
    void shouldFollowWithPlaceHeavyRaiderOnCylonFleetBoardAndAdvancePursuitTrack() {
        setUpGame(Game.builder().build());
        executeAndAssertFollowup(new ActivateHeavyRaidersAndCenturionsEvent(),
                all(new PlaceShipOnCylonFleetBoardEvent(HEAVY_RAIDER),
                        new AdvancePursuitTrackEvent()));
    }

    @Test
    void shouldEndGameIfBoardingPartyReachEnd() {
        galacticaBoard.boardGalactica(centurion)
                .advanceBoardingParty()
                .advanceBoardingParty()
                .advanceBoardingParty();

        executeAndAssertFollowup(new ActivateHeavyRaidersAndCenturionsEvent(), one(new CylonsWinEvent()));
    }

    void boardGalactica(Centurion centurion) {
        galacticaBoard.boardGalactica(centurion);
    }
}
