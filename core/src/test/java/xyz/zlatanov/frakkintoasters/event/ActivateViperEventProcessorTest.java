package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class ActivateViperEventProcessorTest extends EventTestHarness<ActivateViperEvent> {

    /* a pilot activates his own viper (rule 11.2.1) */

    @Test
    void shouldMoveThePilotsViper() {
        val viper = pilot(GALACTICA_SPACE_4_OCLOCK);

        execute(new ActivateViperEvent(1, ViperActivation.move(viper, GALACTICA_SPACE_2_OCLOCK)));

        assertEquals(GALACTICA_SPACE_2_OCLOCK, galacticaBoard.locate(galacticaBoard.pilotedFighter(KARA_STARBUCK_THRACE).orElseThrow()));
    }

    @Test
    void shouldAttackWithThePilotsViper() {
        val viper = pilot(GALACTICA_SPACE_4_OCLOCK);
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ActivateViperEvent(1, ViperActivation.attack(viper, raider.id())));

        assertFollowup(AttackCylonShipEvent.byFighter(viper, raider.id()));
    }

    @Test
    void shouldEscortWithThePilotsViper() {
        val viper = pilot(GALACTICA_SPACE_4_OCLOCK);
        val civilianShip = civilianShipAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ActivateViperEvent(1, ViperActivation.escort(viper, civilianShip.id())));

        assertTrue(galacticaBoard.shipsInSpace(CivilianShip.class).isEmpty());
    }

    @Test
    void shouldNotActivateAnUnmannedViperAsAPilot() {
        pilot(GALACTICA_SPACE_4_OCLOCK);
        val unmanned = viperAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new ActivateViperEvent(1, ViperActivation.move(unmanned.id(), GALACTICA_SPACE_2_OCLOCK)));
    }

    @Test
    void shouldNotLaunchAsAPilot() {
        pilot(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new ActivateViperEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_6_OCLOCK)));
    }

    /* anyone else activates an unmanned viper, e.g. at "Command" */

    @Test
    void shouldLaunchAnUnmannedViper() {
        player(1).character(SAUL_TIGH);

        execute(new ActivateViperEvent(1, ViperActivation.launch(VIPER, GALACTICA_SPACE_6_OCLOCK)));

        assertFollowup(new LaunchViperEvent(1, VIPER, GALACTICA_SPACE_6_OCLOCK));
    }

    @Test
    void shouldMoveAMarkVIITwoSpaceAreas() {
        player(1).character(SAUL_TIGH);
        val markVII = viperMarkVIIAt(GALACTICA_SPACE_4_OCLOCK);

        execute(new ActivateViperEvent(1, ViperActivation.move(markVII.id(), GALACTICA_SPACE_12_OCLOCK)));

        assertEquals(GALACTICA_SPACE_12_OCLOCK, locate(markVII));
    }

    @Test
    void shouldMoveAViperToAnAdjacentSpaceArea() {
        player(1).character(SAUL_TIGH);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new ActivateViperEvent(1, ViperActivation.move(viper.id(), GALACTICA_SPACE_12_OCLOCK)));
    }

    @Test
    void shouldNotActivateAnotherPlayersViper() {
        player(1).character(SAUL_TIGH);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK).pilot(KARA_STARBUCK_THRACE);

        assertInvalid(new ActivateViperEvent(1, ViperActivation.move(viper.id(), GALACTICA_SPACE_2_OCLOCK)));
    }

    @Test
    void shouldNameOneActivation() {
        player(1).character(SAUL_TIGH);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);

        assertInvalid(new ActivateViperEvent(1, new ViperActivation(null, null, viper.id(), GALACTICA_SPACE_2_OCLOCK, raider.id(), null)));
    }

    @Test
    void shouldNameAnActivation() {
        player(1).character(SAUL_TIGH);

        assertInvalid(new ActivateViperEvent(1, null));
    }

    private int pilot(Location area) {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(area);
        ops.player(1).pilot(viper.id());
        return viper.id();
    }
}
