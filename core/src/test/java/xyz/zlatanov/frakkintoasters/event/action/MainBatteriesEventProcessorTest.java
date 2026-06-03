package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MainBatteriesEventProcessorTest extends EventTestHarness<MainBatteriesEvent> {
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
        val civilian = civilianShip();
        place(GALACTICA_SPACE_2_OCLOCK, civilian);
        nextRoll(1);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_2_OCLOCK), new DestroyCivilianShipEvent(civilian.id()));
    }

    @Test
    void shouldFollowUpWithPlayerDecisionOnMultipleCivilianShipOptionsToDestroy() {
        val civilian1 = civilianShip();
        val civilian2 = civilianShip();
        place(GALACTICA_SPACE_2_OCLOCK, civilian1, civilian2);
        nextRoll(1);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_2_OCLOCK), new PlayerDecisionEvent<>(1, DestroyCivilianShipEvent.class));
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipsToDestroy() {
        nextRoll(1);
        executeAndAssertNoFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldDestroyOnlyViperPresent() {
        val testViper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        nextRoll(2);
        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_4_OCLOCK), new DamageHumanFighterEvent(testViper.id()));
    }

    @Test
    void shouldFollowWithDamageViperDecisionWhenMultipleVipersArePresent() {
        place(GALACTICA_SPACE_4_OCLOCK, viper(), viper());
        nextRoll(3);
        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_4_OCLOCK), new PlayerDecisionEvent<>(1, DamageHumanFighterEvent.class));
    }

    @Test
    void shouldDoNothingWhenNoVipers() {
        nextRoll(2);
        executeAndAssertNoFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_4_OCLOCK));
    }

    @Test
    void shouldDoNothingWhenNoRaiders() {
        nextRoll(4);
        executeAndAssertNoFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_6_OCLOCK));
    }

    @Test
    void shouldDoNothingWhenNoRaidersOnHighRoll() {
        nextRoll(8);
        executeAndAssertNoFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_8_OCLOCK));
    }

    @Test
    void shouldFollowupWithDestroy2RaidersEvent() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2);
        nextRoll(4);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_6_OCLOCK), new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id())));
    }

    @Test
    void shouldFollowupWithDestroyRaidersEventWhenOnlyOneIsPresent() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1);
        nextRoll(5);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_6_OCLOCK), new DestroyRaidersEvent(Set.of(raider1.id())));
    }

    @Test
    void shouldFollowWithPlayerDecisionWhen3RaidersArePresent() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2, raider3);
        nextRoll(6);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_6_OCLOCK), new PlayerDecisionEvent<>(1, DestroyRaidersEvent.class));
    }

    @Test
    void shouldFollowupWithDestroy4RaidersEvent() {
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4);
        nextRoll(7);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_8_OCLOCK), new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id(), raider3.id(), raider4.id())));

    }

    @Test
    void shouldFollowWithPlayerDecisionWhen5RaidersArePresent() {
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4, raider());
        nextRoll(8);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_8_OCLOCK), new PlayerDecisionEvent<>(1, DestroyRaidersEvent.class));
    }

    @Test
    void shouldFollowWithDestroyRaidersEventWithTwoRaiders() {
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2);
        nextRoll(7);

        executeAndAssertFollowup(new MainBatteriesEvent(1, GALACTICA_SPACE_8_OCLOCK), new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id())));

    }
}
