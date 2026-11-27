package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterViperAttacked;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;

class AttackViperEventProcessorTest extends EventTestHarness<AttackViperEvent> {

    // "Evasive Maneuvers": "Play after any viper is attacked to reroll the die."
    @Test
    void shouldRollWithEvasiveManoeuvresAfterIt() {
        val raider = raiderAt(GALACTICA_SPACE_5_OCLOCK);
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new AttackViperEvent(raider.id(), viper.id()));

        assertFollowup(rollDie(new BeforeDieRoll(), new AfterViperAttacked(viper.id()), new AttackViperResultEvent(viper.id())));
    }

    @Test
    void shouldAttackAViperInTheRaidersSpaceArea() {
        val raider = raiderAt(GALACTICA_SPACE_5_OCLOCK);
        val viper = viperAt(GALACTICA_SPACE_7_OCLOCK);

        assertInvalid(new AttackViperEvent(raider.id(), viper.id()));
    }
}
