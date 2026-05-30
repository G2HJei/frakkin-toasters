package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MainBatteriesActionEventTest extends EventTest {
    Raider raider1;
    Raider raider2;
    Raider raider3;
    Raider raider4;

    @BeforeEach
    void setUp() {
        raider1 = raider();
        raider2 = raider();
        raider3 = raider();
        raider4 = raider();
    }

    @Test
    void shouldFollowupWithCivilianShipDestruction() {
        val civilian = new CivilianShip(100, 0, 0, 0);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilian);
        die.nextRoll(1);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK));

        assertEquals(single(new DestroyCivilianShipEvent(civilian.id())), followups);
    }

    @Test
    void shouldFollowUpWithPlayerDecisionOnMultipleCivilianShipOptionsToDestroy() {
        val civilian1 = new CivilianShip(100, 0, 0, 0);
        val civilian2 = new CivilianShip(101, 0, 0, 0);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilian1, civilian2);
        die.nextRoll(1);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK));

        assertEquals(single(new PlayerDecisionEvent<>(1, DestroyCivilianShipEvent.class)), followups);
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipsToDestroy() {
        die.nextRoll(1);
        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_2_OCLOCK));
        assertEquals(Followup.NONE, followups);
    }

    @Test
    void shouldDestroyOnlyViperPresent() {
        val testViper = viper();
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, testViper);
        die.nextRoll(2);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK));

        assertEquals(single(new DamageHumanFighterEvent(Set.of(testViper.id()))), followups);
    }

    @Test
    void shouldFollowWithDamageViperDecisionWhenMultipleVipersArePresent() {
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, viper(), viper());
        die.nextRoll(3);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK));

        assertEquals(single(new PlayerDecisionEvent<>(1, DamageHumanFighterEvent.class)), followups);
    }

    @Test
    void shouldDoNothingWhenNoVipers() {
        die.nextRoll(2);
        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_4_OCLOCK));
        assertEquals(Followup.NONE, followups);
    }

    @Test
    void shouldDoNothingWhenNoRaiders() {
        die.nextRoll(4);
        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK));
        assertEquals(Followup.NONE, followups);
    }

    @Test
    void shouldDoNothingWhenNoRaidersOnHighRoll() {
        die.nextRoll(8);
        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK));
        assertEquals(Followup.NONE, followups);
    }

    @Test
    void shouldFollowupWithDestroy2RaidersEvent() {
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2);
        die.nextRoll(4);

        val followup = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK));

        assertEquals(single(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id()))), followup);
    }

    @Test
    void shouldFollowupWithDestroyRaidersEventWhenOnlyOneIsPresent() {
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider1);
        die.nextRoll(5);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK));

        assertEquals(single(new DestroyRaidersEvent(Set.of(raider1.id()))), followups);
    }

    @Test
    void shouldFollowWithPlayerDecisionWhen3RaidersArePresent() {
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2, raider3);
        die.nextRoll(6);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_6_OCLOCK));

        assertEquals(single(new PlayerDecisionEvent<>(1, DestroyRaidersEvent.class)), followups);
    }

    @Test
    void shouldFollowupWithDestroy4RaidersEvent() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4);
        die.nextRoll(7);

        val followup = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK));

        assertEquals(single(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id(), raider3.id(), raider4.id()))), followup);

    }

    @Test
    void shouldFollowWithPlayerDecisionWhen5RaidersArePresent() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4, raider());
        die.nextRoll(8);

        val followups = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK));

        assertEquals(single(new PlayerDecisionEvent<>(1, DestroyRaidersEvent.class)), followups);
    }

    @Test
    void shouldFollowWithDestroyRaidersEventWithTwoRaiders() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2);
        die.nextRoll(7);

        val followup = execute(new MainBatteriesActionEvent(1, GALACTICA_SPACE_8_OCLOCK));

        assertEquals(single(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id()))), followup);

    }
}
