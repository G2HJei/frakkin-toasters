package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamageVipersEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShips;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MainBatteriesActionEventTest {

    FakeDie        die            = new FakeDie();
    Game           game           = Game.builder()
            .die(die)
            .build();
    CylonShips     cylonShips     = game.cylonShips();
    GalacticaBoard galacticaBoard = game.boards().galactica();

    @Test
    void shouldFollowupWithCivilianShipDestruction() {
        val civilian = new CivilianShip(100, 0, 0, 0);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilian);
        die.nextRoll(1);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK).execute(game);

        assertEquals(followWith(new DestroyCivilianShipEvent(civilian.id())), followups);
    }

    @Test
    void shouldFollowUpWithPlayerDecisionOnMultipleCivilianShipOptionsToDestroy() {
        val civilian1 = new CivilianShip(100, 0, 0, 0);
        val civilian2 = new CivilianShip(101, 0, 0, 0);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilian1, civilian2);
        die.nextRoll(1);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK).execute(game);

        assertEquals(followWith(new PlayerDecisionEvent(1, DestroyCivilianShipEvent.class)), followups);
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipsToDestroy() {
        die.nextRoll(1);
        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK).execute(game);
        assertTrue(followups.isEmpty());
    }

    @Test
    void shouldDestroyOnlyViperPresent() {
        val viper = new Viper(50);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper);
        die.nextRoll(2);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK).execute(game);

        assertEquals(followWith(new DamageVipersEvent(Set.of(viper.id()))), followups);
    }

    @Test
    void shouldFollowWithDamageViperDecisionWhenMultipleVipersArePresent() {
        val viper1 = new Viper(50);
        val viper2 = new Viper(51);
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper1, viper2);
        die.nextRoll(3);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK).execute(game);

        assertEquals(followWith(new PlayerDecisionEvent(1, DamageVipersEvent.class)), followups);
    }

    @Test
    void shouldDoNothingWhenNoVipers() {
        die.nextRoll(2);
        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK).execute(game);
        assertTrue(followups.isEmpty());
    }

    @Test
    void shouldDoNothingWhenNoRaiders() {
        die.nextRoll(4);
        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK).execute(game);
        assertTrue(followups.isEmpty());
    }

    @Test
    void shouldDoNothingWhenNoRaidersOnHighRoll() {
        die.nextRoll(8);
        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK).execute(game);
        assertTrue(followups.isEmpty());
    }

    @Test
    void shouldFollowupWithDestroy2RaidersEvent() {
        val raider1 = cylonShips.raider();
        val raider2 = cylonShips.raider();
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2);
        die.nextRoll(4);

        val followup = new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK).execute(game);

        assertEquals(followWith(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id()))), followup);
    }

    @Test
    void shouldFollowupWithDestroyRaidersEventWhenOnlyOneIsPresent() {
        val raider = cylonShips.raider();
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider);
        die.nextRoll(5);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK).execute(game);

        assertEquals(followWith(new DestroyRaidersEvent(Set.of(raider.id()))), followups);
    }

    @Test
    void shouldFollowWithPlayerDecisionWhen3RaidersArePresent() {
        val raider1 = cylonShips.raider();
        val raider2 = cylonShips.raider();
        val raider3 = cylonShips.raider();
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2, raider3);
        die.nextRoll(6);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK).execute(game);

        assertEquals(followWith(new PlayerDecisionEvent(1, DestroyRaidersEvent.class)), followups);
    }

    @Test
    void shouldFollowupWithDestroy4RaidersEvent() {
        val raider1 = cylonShips.raider();
        val raider2 = cylonShips.raider();
        val raider3 = cylonShips.raider();
        val raider4 = cylonShips.raider();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4);
        die.nextRoll(7);

        val followup = new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK).execute(game);

        assertEquals(followWith(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id(), raider3.id(), raider4.id()))), followup);

    }

    @Test
    void shouldFollowWithPlayerDecisionWhen5RaidersArePresent() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK,
                cylonShips.raider(),
                cylonShips.raider(),
                cylonShips.raider(),
                cylonShips.raider(),
                cylonShips.raider());
        die.nextRoll(8);

        val followups = new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK).execute(game);

        assertEquals(followWith(new PlayerDecisionEvent(1, DestroyRaidersEvent.class)), followups);
    }

    @Test
    void shouldFollowWithDestroyRaidersEventWithTwoRaiders() {
        val raider1 = cylonShips.raider();
        val raider2 = cylonShips.raider();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2);
        die.nextRoll(7);

        val followup = new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK).execute(game);

        assertEquals(followWith(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id()))), followup);

    }
}
