package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUANNE_KAT_KATRAINE;

class ActivateRaiderEventProcessorTest extends EventTestHarness<ActivateRaiderEvent> {

    Raider              raider;
    CivilianShip        civilianShip;
    CivilianShip        secondCivilianShip;
    Viper               unmannedViper;
    Viper               pilotedViper;
    AssaultRaptor       pilotedAssaultRaptor;
    ActivateRaiderEvent event;

    @BeforeEach
    void setUp() {
        raider = raider();
        civilianShip = civilianShip();
        secondCivilianShip = civilianShip();
        unmannedViper = viper();
        pilotedViper = viper().pilot(KARA_STARBUCK_THRACE);
        pilotedAssaultRaptor = assaultRaptor().pilot(LOUANNE_KAT_KATRAINE);
        event = new ActivateRaiderEvent(raider.id());
    }

    @Test
    void shouldAttackGalacticaWhenNoCivilianShipsOnBoard() {
        place(GALACTICA_SPACE_4_OCLOCK, raider);
        executeAndAssertFollowup(new ActivateRaiderEvent(raider.id()), new AttackGalacticaEvent(raider.id()));
    }

    @Test
    void shouldMoveRaiderTowardNearestCivilianShip() {
        place(GALACTICA_SPACE_12_OCLOCK, raider);
        place(GALACTICA_SPACE_4_OCLOCK, civilianShip);
        executeAndAssertNoFollowup(event);
        assertEquals(GALACTICA_SPACE_2_OCLOCK, locate(raider));
    }

    @Test
    void shouldMoveClockwiseWhenEquidistantCivilianShips() {
        place(GALACTICA_SPACE_12_OCLOCK, raider);
        place(GALACTICA_SPACE_2_OCLOCK, civilianShip);
        place(GALACTICA_SPACE_10_OCLOCK, secondCivilianShip);
        executeAndAssertNoFollowup(event);
        assertEquals(GALACTICA_SPACE_2_OCLOCK, locate(raider));
    }

    @Test
    void shouldMoveRaiderTowardNearestCivilianCounterClockwise() {
        place(GALACTICA_SPACE_12_OCLOCK, raider);
        place(GALACTICA_SPACE_10_OCLOCK, civilianShip);
        executeAndAssertNoFollowup(event);
        assertEquals(GALACTICA_SPACE_10_OCLOCK, locate(raider));
    }

    @Test
    void shouldDestroyCivilianShipWhenNoVipersInArea() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, civilianShip);
        executeAndAssertFollowup(new ActivateRaiderEvent(raider.id()), new DestroyCivilianShipEvent(civilianShip.id()));
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(raider));
    }

    @Test
    void shouldLetPlayerChooseWhenMultipleCivilianShipsInArea() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, civilianShip, secondCivilianShip);
        executeAndAssertFollowup(
                new ActivateRaiderEvent(raider.id()),
                one(
                        new DestroyCivilianShipEvent(civilianShip.id()),
                        new DestroyCivilianShipEvent(secondCivilianShip.id())));
    }

    @Test
    void shouldAttackUnmannedViperFirst() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, unmannedViper, pilotedViper);
        executeAndAssertFollowup(new ActivateRaiderEvent(raider.id()), new AttackViperEvent(raider.id(), unmannedViper.id()));
    }

    @Test
    void shouldAttackPilotedViperWhenNoUnmannedVipers() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, pilotedViper);
        executeAndAssertFollowup(new ActivateRaiderEvent(raider.id()), new AttackViperEvent(raider.id(), pilotedViper.id()));
    }

    @Test
    void shouldLetPlayerChooseBetweenTwoPilotedVipers() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, pilotedViper, pilotedAssaultRaptor);

        executeAndAssertFollowup(
                new ActivateRaiderEvent(raider.id()),
                one(
                        new AttackViperEvent(raider.id(), pilotedViper.id()),
                        new AttackViperEvent(raider.id(), pilotedAssaultRaptor.id())));
    }

    @Test
    void shouldPreferViperOverCivilianShip() {
        place(GALACTICA_SPACE_4_OCLOCK, raider, unmannedViper, civilianShip);
        executeAndAssertFollowup(new ActivateRaiderEvent(raider.id()), new AttackViperEvent(raider.id(), unmannedViper.id()));
    }

}