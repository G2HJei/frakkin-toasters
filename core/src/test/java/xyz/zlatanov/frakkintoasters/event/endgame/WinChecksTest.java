package xyz.zlatanov.frakkintoasters.event.endgame;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class WinChecksTest extends TestHarness {

    private static final List<Location> SIX_LOCATIONS =
            List.of(FTL_CONTROL, WEAPONS_CONTROL, COMMAND, ADMIRALS_QUARTERS, HANGAR_DECK, ARMORY);

    @Test
    void nobodyWinsAtTheStart() {
        assertEquals(NONE, WinChecks.check(game));
    }

    @Test
    void theCylonsWinWhenACenturionReachesTheEndOfTheBoardingPartyTrack() {
        galacticaBoard.boardGalactica(centurion())
                .advanceBoardingParty()
                .advanceBoardingParty()
                .advanceBoardingParty();
        assertEquals(NONE, WinChecks.check(game));

        galacticaBoard.advanceBoardingParty();

        assertEquals(single(new CylonsWinEvent()), WinChecks.check(game));
    }

    @Test
    void theCylonsWinWhenSixGalacticaLocationsAreDamaged() {
        SIX_LOCATIONS.subList(0, 5).forEach(galacticaBoard::damage);
        pegasusBoard.damage(PEGASUS_CIC); // not a Galactica location
        assertEquals(NONE, WinChecks.check(game));

        galacticaBoard.damage(ARMORY);

        assertEquals(single(new CylonsWinEvent()), WinChecks.check(game));
    }

    @Test
    void aGameThatEndedWinsNoMore() {
        SIX_LOCATIONS.forEach(galacticaBoard::damage);
        game.end(CYLONS);

        assertEquals(NONE, WinChecks.check(game));
    }
}
