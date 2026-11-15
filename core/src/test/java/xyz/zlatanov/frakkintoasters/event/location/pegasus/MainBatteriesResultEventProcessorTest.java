package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Arrays;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MainBatteriesResultEventProcessorTest extends EventTestHarness<MainBatteriesResultEvent> {
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
        rolled(1);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_2_OCLOCK));

        assertFollowup(new DestroyCivilianShipEvent(civilian.id()));
    }

    @Test
    void shouldChooseACivilianShipInTheSpaceArea() {
        val civilian1 = civilianShip();
        val civilian2 = civilianShip();
        place(GALACTICA_SPACE_2_OCLOCK, civilian1, civilian2);
        civilianShipAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(1);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_2_OCLOCK));

        val first = civilian1.id() < civilian2.id() ? civilian1 : civilian2;
        val second = first == civilian1 ? civilian2 : civilian1;
        assertFollowup(one(new DestroyCivilianShipEvent(first.id()), new DestroyCivilianShipEvent(second.id())));
    }

    @Test
    void shouldDoNothingWhenNoCivilianShipsToDestroy() {
        rolled(1);
        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_2_OCLOCK));
    }

    @Test
    void shouldDestroyOnlyViperPresent() {
        val testViper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(2);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_4_OCLOCK));

        assertFollowup(new DamageHumanFighterEvent(testViper.id()));
    }

    @Test
    void shouldChooseAViperInTheSpaceArea() {
        val viper1 = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val viper2 = viperAt(GALACTICA_SPACE_4_OCLOCK);
        viperAt(GALACTICA_SPACE_6_OCLOCK);
        rolled(3);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_4_OCLOCK));

        val first = viper1.id() < viper2.id() ? viper1 : viper2;
        val second = first == viper1 ? viper2 : viper1;
        assertFollowup(one(new DamageHumanFighterEvent(first.id()), new DamageHumanFighterEvent(second.id())));
    }

    @Test
    void shouldDoNothingWhenNoVipers() {
        rolled(2);
        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_4_OCLOCK));
    }

    @Test
    void shouldDoNothingWhenNoRaiders() {
        rolled(4);
        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_6_OCLOCK));
    }

    @Test
    void shouldDoNothingWhenNoRaidersOnHighRoll() {
        rolled(8);
        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_8_OCLOCK));
    }

    @Test
    void shouldFollowupWithDestroy2RaidersEvent() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2);
        rolled(4);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_6_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id())));
    }

    @Test
    void shouldFollowupWithDestroyRaidersEventWhenOnlyOneIsPresent() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1);
        rolled(5);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_6_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(Set.of(raider1.id())));
    }

    @Test
    void shouldDestroy2Of3Raiders() {
        place(GALACTICA_SPACE_6_OCLOCK, raider1, raider2, raider3);
        rolled(6);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_6_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(lowestIds(2, raider1, raider2, raider3)));
    }

    @Test
    void shouldFollowupWithDestroy4RaidersEvent() {
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4);
        rolled(7);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_8_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id(), raider3.id(), raider4.id())));

    }

    @Test
    void shouldDestroy4Of5Raiders() {
        val raider5 = raider();
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2, raider3, raider4, raider5);
        rolled(8);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_8_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(lowestIds(4, raider1, raider2, raider3, raider4, raider5)));
    }

    @Test
    void shouldFollowWithDestroyRaidersEventWithTwoRaiders() {
        place(GALACTICA_SPACE_8_OCLOCK, raider1, raider2);
        rolled(7);

        execute(new MainBatteriesResultEvent(GALACTICA_SPACE_8_OCLOCK));

        assertFollowup(new DestroyRaidersEvent(Set.of(raider1.id(), raider2.id())));

    }

    @Test
    void shouldOnlyAffectAGalacticaSpaceArea() {
        assertInvalid(new MainBatteriesResultEvent(COMMAND));
    }

    @Test
    void shouldChooseASpaceArea() {
        assertInvalid(new MainBatteriesResultEvent(null));
    }

    private static Set<Integer> lowestIds(int count, Raider... raiders) {
        return Arrays.stream(raiders)
                .sorted(comparingInt(Ship::id))
                .limit(count)
                .map(Ship::id)
                .collect(toSet());
    }
}
