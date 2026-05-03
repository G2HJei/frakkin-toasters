package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class ActivateRaidersEventTest {

    Game           game           = Game.builder().build();
    GalacticaBoard galacticaBoard = game.boards().galactica();


    @Test
    void shouldPlaceRaiderOnCylonFleetBoardWhenNoRaidersOrBasestarsOnMainBoard() {
        val followup = executeEvent();
        assertEquals(
                all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()),
                followup);
    }


    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        val basestar = game.cylonShips().basestar();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        val raiders = galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, RAIDER);
        assertEquals(2, raiders.size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        val basestar1 = game.cylonShips().basestar();
        val basestar2 = game.cylonShips().basestar();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar1);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar2);

        executeEvent();

        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, RAIDER).size());
        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, RAIDER).size());
    }

    @Test
    void shouldActivateRaidersOneByOne() {
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();

        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, raider1);
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider2);

        val followup = executeEvent();

        assertEquals(all(
                        new ActivateRaiderEvent(raider1.id()),
                        new ActivateRaiderEvent(raider2.id()))
                , followup);
    }

    private Followup executeEvent() {
        return new ActivateRaidersEvent().execute(game);
    }

    /// ////////////////////////////////////////
    /// // todo move all below to ActivateRaiderEventTest
    //@Test
    void shouldAttackGalacticaWhenNoCivilianShipsOnBoard() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);

        val followup = new ActivateRaidersEvent().execute(game);

        //todo change to AttackGalacticaEvent
        assertEquals(single(new DamageGalacticaEvent()), followup);
    }
    //todo wrong test, correct it and then fix behavior!
    //@Test

    void shouldAttackGalacticaOnceEvenWithMultipleRaiders() {
        val game = Game.builder().build();
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider1);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider2);

        val followup = new ActivateRaidersEvent().execute(game);

        // TODO each raider attacks galactica. Change this to AttackGalacticaEvents
        assertEquals(all(new DamageGalacticaEvent(), new DamageGalacticaEvent()), followup);
    }
    //todo add test when one raider attacks galactica, other attacks viper, third attacks manned viper, fourth attacks civ ship

    //todo add test when one raider destroys civ ship, then the other no longer has target and moves. This should actually refactor the whole event into activate raiders with id of specific raider being activated
    //@Test

    void shouldMoveRaiderTowardNearestCivilianShip() {
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_12_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian);

        executeEvent();

        assertEquals(GALACTICA_SPACE_2_OCLOCK, galacticaBoard.locate(raider));
    }
    //@Test

    void shouldMoveClockwiseWhenEquidistantCivilianShips() {
        val raider = game.cylonShips().raider();
        val civilian1 = new CivilianShip(500, 0, 0, 1);
        val civilian2 = new CivilianShip(501, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_12_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilian1);
        galacticaBoard.place(GALACTICA_SPACE_10_OCLOCK, civilian2);

        executeEvent();

        assertEquals(GALACTICA_SPACE_2_OCLOCK, galacticaBoard.locate(raider));
    }
    //@Test

    void shouldMoveRaiderTowardNearestCivilianCounterClockwise() {
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_12_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_10_OCLOCK, civilian);

        executeEvent();

        assertEquals(GALACTICA_SPACE_10_OCLOCK, galacticaBoard.locate(raider));
    }
    //@Test

    void shouldNotMoveRaiderIfCivilianShipInSameArea() {
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian);

        executeEvent();

        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(raider));
    }

    //@Test

    void shouldDestroyCivilianShipWhenNoVipersInArea() {
        val raider = game.cylonShips().raider();
        val civilian = new CivilianShip(500, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian);

        val followup = executeEvent();

        assertEquals(single(new DestroyCivilianShipEvent(500)), followup);
    }
    //@Test

    void shouldLetPlayerChooseWhenMultipleCivilianShipsInArea() {
        val raider = game.cylonShips().raider();
        val civilian1 = new CivilianShip(500, 0, 0, 1);
        val civilian2 = new CivilianShip(501, 0, 1, 0);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian2);

        val followup = executeEvent();

        assertEquals(one(new DestroyCivilianShipEvent(500), new DestroyCivilianShipEvent(501)), followup);
    }

    // === Rule 1: Attack a Viper ===
    //@Test

    void shouldAttackUnmannedViperFirst() {
        val game = Game.builder().build();
        val raider = game.cylonShips().raider();
        val unmannedViper = new Viper(500);
        val pilotedViper = new Viper(501).pilot(KARA_STARBUCK_THRACE);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, unmannedViper);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);

        val followup = executeEvent();

        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }
    //@Test

    void shouldAttackPilotedViperWhenNoUnmannedVipers() {
        val raider = game.cylonShips().raider();
        val pilotedViper = new Viper(500).pilot(KARA_STARBUCK_THRACE);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);

        val followup = executeEvent();

        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }
    //@Test

    void shouldLetPlayerChooseBetweenTwoPilotedVipers() {
        val raider = game.cylonShips().raider();
        val pilotedViper1 = new Viper(500).pilot(KARA_STARBUCK_THRACE);
        val pilotedViper2 = new Viper(501).pilot(LEE_APOLLO_ADAMA);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, pilotedViper1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, pilotedViper2);

        val followup = executeEvent();

        assertEquals(
                one(new DamageVipersEvent(java.util.Set.of(500)), new DamageVipersEvent(java.util.Set.of(501))),
                followup);
    }
    //@Test

    void shouldPreferViperOverCivilianShip() {
        val raider = game.cylonShips().raider();
        val viper = new Viper(500);
        val civilian = new CivilianShip(501, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, civilian);

        val followup = executeEvent();

        assertEquals(single(new DamageVipersEvent(java.util.Set.of(500))), followup);
    }

    //@Test

    void shouldHandleMultipleRaidersInDifferentAreas() {
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        val viper = new Viper(500);
        val civilian = new CivilianShip(501, 0, 0, 1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider2);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, civilian);

        val followup = executeEvent();

        assertEquals(
                //todo should be attack viper not damage, and destroy should be separate test
                all(new DamageVipersEvent(Set.of(500)), new DestroyCivilianShipEvent(501)),
                followup);
    }
    //@Test

    void shouldHandleMultipleRaidersAttackingSameUnmannedViper() {
        val raider1 = game.cylonShips().raider();
        val raider2 = game.cylonShips().raider();
        val viper = new Viper(500);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider1);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider2);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper);

        val followup = executeEvent();

        assertEquals(all(new DamageVipersEvent(java.util.Set.of(500)), new DamageVipersEvent(java.util.Set.of(500))), followup);
    }
}
