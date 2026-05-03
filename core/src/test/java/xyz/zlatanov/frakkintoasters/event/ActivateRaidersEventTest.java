package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class ActivateRaidersEventTest {

    @Test
    void shouldPlaceRaiderOnCylonFleetBoardWhenNoRaidersOrBasestarsOnMainBoard() {
        val game = Game.builder().build();
        // no raiders, no basestars on main board
        val followup = new ActivateRaidersEvent().execute(game);
        assertEquals(
                all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()),
                followup);
    }

    // === Rule 5: Launch ===

    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        val game = Game.builder().build();
        val basestar = game.cylonShips().basestar();
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = new ActivateRaidersEvent().execute(game);

        val raiders = game.boards().galactica().shipsIn(GALACTICA_SPACE_8_OCLOCK, RAIDER);
        assertEquals(2, raiders.size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        val game = Game.builder().build();
        val basestar1 = game.cylonShips().basestar();
        val basestar2 = game.cylonShips().basestar();
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, basestar1);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, basestar2);

        new ActivateRaidersEvent().execute(game);

        assertEquals(2, game.boards().galactica().shipsIn(GALACTICA_SPACE_8_OCLOCK, RAIDER).size());
        assertEquals(2, game.boards().galactica().shipsIn(GALACTICA_SPACE_2_OCLOCK, RAIDER).size());
    }

    // === Rule 4: Attack Galactica ===

    @Test
    void shouldAttackGalacticaWhenNoCivilianShipsOnBoard() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        // no civilians anywhere

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(single(new DamageGalacticaEvent()), followup);
    }

    @Test
    void shouldAttackGalacticaOnceEvenWithMultipleRaiders() {
        val game = Game.builder().build();
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider1);
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, raider2);

        val followup = new ActivateRaidersEvent().execute(game);

        // each raider attacks galactica
        assertEquals(all(new DamageGalacticaEvent(), new DamageGalacticaEvent()), followup);
    }

    // === Rule 3: Move toward nearest civilian ship ===

    @Test
    void shouldMoveRaiderTowardNearestCivilianShip() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_12_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian);

        new ActivateRaidersEvent().execute(game);

        assertEquals(GALACTICA_SPACE_2_OCLOCK, game.boards().galactica().locate(raider));
    }

    @Test
    void shouldMoveClockwiseWhenEquidistantCivilianShips() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian1 = new CivilianShip(500, 0, 0, 1);
        val civilian2 = new CivilianShip(501, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_12_OCLOCK, raider);
        // equidistant: 4 o'clock (2 steps clockwise) and 8 o'clock (2 steps counter-clockwise)
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian1);
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, civilian2);

        new ActivateRaidersEvent().execute(game);

        // should move clockwise toward 4 o'clock
        assertEquals(GALACTICA_SPACE_2_OCLOCK, game.boards().galactica().locate(raider));
    }

    @Test
    void shouldMoveRaiderTowardNearestCivilianCounterClockwise() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_12_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_10_OCLOCK, civilian);

        new ActivateRaidersEvent().execute(game);

        assertEquals(GALACTICA_SPACE_10_OCLOCK, game.boards().galactica().locate(raider));
    }

    @Test
    void shouldNotMoveRaiderIfCivilianShipInSameArea() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        // raider and civilian in same area - raider should destroy civilian (rule 2), not move
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian);

        val followup = new ActivateRaidersEvent().execute(game);

        // should destroy civilian, not move
        assertEquals(GALACTICA_SPACE_4_OCLOCK, game.boards().galactica().locate(raider));
    }

    // === Rule 2: Destroy Civilian Ship ===

    @Test
    void shouldDestroyCivilianShipWhenNoVipersInArea() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian);

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(single(new DestroyCivilianShipEvent(500)), followup);
    }

    @Test
    void shouldLetPlayerChooseWhenMultipleCivilianShipsInArea() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val civilian1 = new CivilianShip(500, 0, 0, 1);
        val civilian2 = new CivilianShip(501, 0, 1, 0);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian2);

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(one(new DestroyCivilianShipEvent(500), new DestroyCivilianShipEvent(501)), followup);
    }

    // === Rule 1: Attack a Viper ===

    @Test
    void shouldAttackUnmannedViperFirst() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val unmannedViper = new Viper(500);
        val pilotedViper = new Viper(501).pilot(KARA_STARBUCK_THRACE);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, unmannedViper);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }

    @Test
    void shouldAttackPilotedViperWhenNoUnmannedVipers() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val pilotedViper = new Viper(500).pilot(KARA_STARBUCK_THRACE);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }

    @Test
    void shouldLetPlayerChooseBetweenTwoPilotedVipers() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val pilotedViper1 = new Viper(500).pilot(KARA_STARBUCK_THRACE);
        val pilotedViper2 = new Viper(501).pilot(LEE_APOLLO_ADAMA);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, pilotedViper1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, pilotedViper2);

        val followup = new ActivateRaidersEvent().execute(game);

        assertEquals(
                one(new DamageVipersEvent(java.util.Set.of(500)), new DamageVipersEvent(java.util.Set.of(501))),
                followup);
    }

    @Test
    void shouldPreferViperOverCivilianShip() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val viper = new Viper(500);
        val civilian = new CivilianShip(501, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, viper);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, civilian);

        val followup = new ActivateRaidersEvent().execute(game);

        // attack viper, not destroy civilian
        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }

    // === Multiple raiders in different areas ===

    @Test
    void shouldHandleMultipleRaidersInDifferentAreas() {
        val game = Game.builder().build();
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        val viper = new Viper(500);
        val civilian = new CivilianShip(501, 0, 0, 1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, viper);
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, raider2);
        game.boards().galactica().place(GALACTICA_SPACE_8_OCLOCK, civilian);

        val followup = new ActivateRaidersEvent().execute(game);

        // raider1 attacks viper, raider2 destroys civilian
        assertEquals(
                all(new DamageVipersEvent(java.util.Set.of(500)), new DestroyCivilianShipEvent(501)),
                followup);
    }

    @Test
    void shouldHandleMultipleRaidersAttackingSameUnmannedViper() {
        val game = Game.builder().build();
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        val viper = new Viper(500);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider1);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, raider2);
        game.boards().galactica().place(GALACTICA_SPACE_4_OCLOCK, viper);

        val followup = new ActivateRaidersEvent().execute(game);

        // both raiders attack the same unmanned viper
        assertEquals(all(new DamageVipersEvent(java.util.Set.of(500)), new DamageVipersEvent(java.util.Set.of(500))), followup);
    }
}
