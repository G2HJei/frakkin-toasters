package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforePilotedViperAttack;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class AttackCylonShipEventProcessorTest extends EventTestHarness<AttackCylonShipEvent> {

    @Test
    void shouldRollForGalacticasAttack() {
        val raider = raiderAt(GALACTICA_SPACE_11_OCLOCK);

        execute(AttackCylonShipEvent.byGalactica(raider.id()));

        assertFollowup(rollDie(new AttackCylonShipResultEvent(null, raider.id())));
    }

    @Test
    void shouldRollForAnUnmannedVipersAttack() {
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_5_OCLOCK);

        execute(AttackCylonShipEvent.byFighter(viper.id(), basestar.id()));

        assertFollowup(rollDie(new AttackCylonShipResultEvent(viper.id(), basestar.id())));
    }

    // "Best of the Best": "Play before a piloted viper makes an attack."
    @Test
    void shouldRollWithBestOfTheBestBeforeAPilotedVipersAttack() {
        player(1).character(KARA_STARBUCK_THRACE);
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.player(1).pilot(viper.id());
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_5_OCLOCK);

        execute(AttackCylonShipEvent.byFighter(viper.id(), heavyRaider.id()));

        assertFollowup(rollDie(new BeforePilotedViperAttack(), new AfterDieRoll(),
                new AttackCylonShipResultEvent(viper.id(), heavyRaider.id())));
    }

    @Test
    void shouldAttackACylonShipInTheFightersSpaceArea() {
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        val raider = raiderAt(GALACTICA_SPACE_7_OCLOCK);

        assertInvalid(AttackCylonShipEvent.byFighter(viper.id(), raider.id()));
    }

    @Test
    void shouldAttackOnlyCylonShips() {
        assertInvalid(AttackCylonShipEvent.byGalactica(civilianShipAt(GALACTICA_SPACE_5_OCLOCK).id()));
    }

    @Test
    void shouldAttackWithAFighterInSpace() {
        val raider = raiderAt(GALACTICA_SPACE_5_OCLOCK);

        assertInvalid(AttackCylonShipEvent.byFighter(viper().id(), raider.id()));
    }
}
