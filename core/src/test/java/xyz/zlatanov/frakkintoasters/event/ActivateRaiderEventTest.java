package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

class ActivateRaiderEventTest {

    Game           game                 = Game.builder().build();
    GalacticaBoard galacticaBoard       = game.boards().galactica();
    Raider         raider               = game.cylonShips().raider();
    CivilianShip   civilianShip         = game.decks().civilianShips().draw();
    Viper          unmannedViper        = (Viper) galacticaBoard.removeFromReserves(VIPER);
    Viper          pilotedViper         = ((Viper) galacticaBoard.removeFromReserves(VIPER)).pilot(KARA_STARBUCK_THRACE);
    AssaultRaptor  pilotedAssaultRaptor = ((AssaultRaptor) galacticaBoard.removeFromReserves(ASSAULT_RAPTOR)).pilot(LOUANNE_KAT_KATRAINE);

    @Test
    void shouldAttackGalacticaWhenNoCivilianShipsOnBoard() {
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, raider);
        val followup = new ActivateRaidersEvent().execute(game);
        assertEquals(single(new AttackGalacticaEvent(raider.id())), followup);
    }

    @Test
    void shouldMoveRaiderTowardNearestCivilianShip() {
        galacticaBoard
                .place(GALACTICA_SPACE_12_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, civilianShip);
        executeEvent();
        assertEquals(GALACTICA_SPACE_2_OCLOCK, galacticaBoard.locate(raider));
    }

    @Test
    void shouldMoveClockwiseWhenEquidistantCivilianShips() {
        val secondCivilianShip = game.decks().civilianShips().draw();
        galacticaBoard
                .place(GALACTICA_SPACE_12_OCLOCK, raider)
                .place(GALACTICA_SPACE_2_OCLOCK, civilianShip)
                .place(GALACTICA_SPACE_10_OCLOCK, secondCivilianShip);
        executeEvent();
        assertEquals(GALACTICA_SPACE_2_OCLOCK, galacticaBoard.locate(raider));
    }

    @Test
    void shouldMoveRaiderTowardNearestCivilianCounterClockwise() {
        galacticaBoard
                .place(GALACTICA_SPACE_12_OCLOCK, raider)
                .place(GALACTICA_SPACE_10_OCLOCK, civilianShip);
        executeEvent();
        assertEquals(GALACTICA_SPACE_10_OCLOCK, galacticaBoard.locate(raider));
    }

    @Test
    void shouldDestroyCivilianShipWhenNoVipersInArea() {
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, civilianShip);
        val followup = executeEvent();
        assertEquals(single(new DestroyCivilianShipEvent(civilianShip.id())), followup);
        assertEquals(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.locate(raider));
    }

    @Test
    void shouldLetPlayerChooseWhenMultipleCivilianShipsInArea() {
        val secondCivilian = game.decks().civilianShips().draw();
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, civilianShip)
                .place(GALACTICA_SPACE_4_OCLOCK, secondCivilian);
        val followup = executeEvent();
        assertEquals(
                one(
                        new DestroyCivilianShipEvent(civilianShip.id()),
                        new DestroyCivilianShipEvent(secondCivilian.id())),
                followup);
    }

    @Test
    void shouldAttackUnmannedViperFirst() {
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, unmannedViper)
                .place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);
        val followup = executeEvent();
        assertEquals(single(new AttackViperEvent(raider.id(), unmannedViper.id())), followup);
    }

    @Test
    void shouldAttackPilotedViperWhenNoUnmannedVipers() {
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, pilotedViper);
        val followup = executeEvent();
        assertEquals(single(new AttackViperEvent(raider.id(), pilotedViper.id())), followup);
    }

    @Test
    void shouldLetPlayerChooseBetweenTwoPilotedVipers() {
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, pilotedViper)
                .place(GALACTICA_SPACE_4_OCLOCK, pilotedAssaultRaptor);

        val followup = executeEvent();

        assertEquals(
                one(
                        new AttackViperEvent(raider.id(), pilotedViper.id()),
                        new AttackViperEvent(raider.id(), pilotedAssaultRaptor.id())),
                followup);
    }

    @Test
    void shouldPreferViperOverCivilianShip() {
        galacticaBoard
                .place(GALACTICA_SPACE_4_OCLOCK, raider)
                .place(GALACTICA_SPACE_4_OCLOCK, unmannedViper)
                .place(GALACTICA_SPACE_4_OCLOCK, civilianShip);
        val followup = executeEvent();
        assertEquals(single(new AttackViperEvent(raider.id(), unmannedViper.id())), followup);
    }

    Followup executeEvent() {
        return new ActivateRaiderEvent(raider.id()).execute(game);
    }
}