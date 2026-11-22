package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BEST_OF_THE_BEST;

class AttackCylonShipResultEventProcessorTest extends EventTestHarness<AttackCylonShipResultEvent> {

    @ParameterizedTest
    @CsvSource({"2, 1", "3, 0"})
    void shouldDestroyARaiderOn3OrHigher(int roll, int raidersLeft) {
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(roll);

        execute(new AttackCylonShipResultEvent(null, raider.id()));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, raidersLeft);
    }

    // The roll's "Hotshot" discard drew a second Mutiny Card and sent the pilot to the "Brig", his viper with him
    // (F-3bdf77).
    @Test
    void shouldMissWhenTheFighterLeftSpaceBeforeTheResult() {
        val raider = raiderAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(8);

        execute(new AttackCylonShipResultEvent(viper().id(), raider.id()));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 1);
    }

    @ParameterizedTest
    @CsvSource({"6, 1", "7, 0"})
    void shouldDestroyAHeavyRaiderOn7OrHigher(int roll, int heavyRaidersLeft) {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(roll);

        execute(new AttackCylonShipResultEvent(viper.id(), heavyRaider.id()));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, 1 + heavyRaidersLeft);
    }

    @Test
    void shouldDamageABasestarWithGalacticaOn5OrHigher() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(5);

        execute(new AttackCylonShipResultEvent(null, basestar.id()));

        assertFollowup(new DamageBasestarEvent(basestar.id()));
    }

    @Test
    void shouldMissABasestarWithGalacticaBelow5() {
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(4);

        execute(new AttackCylonShipResultEvent(null, basestar.id()));
    }

    @Test
    void shouldDamageABasestarWithAViperOn8() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(8);

        execute(new AttackCylonShipResultEvent(viper.id(), basestar.id()));

        assertFollowup(new DamageBasestarEvent(basestar.id()));
    }

    @Test
    void shouldMissABasestarWithAViperBelow8() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(7);

        execute(new AttackCylonShipResultEvent(viper.id(), basestar.id()));
    }

    @Test
    void shouldDamageABasestarWithAnAssaultRaptorOn7OrHigher() {
        val assaultRaptor = assaultRaptorAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(7);

        execute(new AttackCylonShipResultEvent(assaultRaptor.id(), basestar.id()));

        assertFollowup(new DamageBasestarEvent(basestar.id()));
    }

    // "Structural Damage": "all attacks against the basestar gain +2 to their die rolls"
    @Test
    void shouldAdd2AgainstABasestarWithStructuralDamage() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        basestar.damage(STRUCTURAL_DAMAGE);
        rolled(6);

        execute(new AttackCylonShipResultEvent(viper.id(), basestar.id()));

        assertFollowup(new DamageBasestarEvent(basestar.id()));
    }

    // "Instead of rolling the attack, roll a die and destroy that many raiders in the viper's space area."
    @Test
    void shouldDestroyAsManyRaidersAsTheRollWithBestOfTheBest() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_4_OCLOCK);
        val raiderIds = Stream.of(raiderAt(GALACTICA_SPACE_4_OCLOCK), raiderAt(GALACTICA_SPACE_4_OCLOCK), raiderAt(GALACTICA_SPACE_4_OCLOCK))
                .map(Ship::id)
                .sorted()
                .toList();
        ops.startDieRoll();
        ops.playOnDieRoll(BEST_OF_THE_BEST, 0);
        nextRoll(2);
        ops.rollDie();

        execute(new AttackCylonShipResultEvent(viper.id(), basestar.id()));

        assertFollowup(new DestroyRaidersEvent(Set.of(raiderIds.get(0), raiderIds.get(1))));
        assertEquals(List.of(), basestar.damage());
    }
}
