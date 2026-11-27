package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;

class AttackGalacticaEventProcessorTest extends EventTestHarness<AttackGalacticaEvent> {

    @Test
    void shouldRollForTheAttack() {
        int basestarId = basestarAt(GALACTICA_SPACE_5_OCLOCK).id();

        execute(new AttackGalacticaEvent(basestarId));

        assertFollowup(rollDie(new AttackGalacticaResultEvent(basestarId)));
    }

    @Test
    void shouldAttackWithARaiderOrABasestar() {
        assertInvalid(new AttackGalacticaEvent(heavyRaiderAt(GALACTICA_SPACE_5_OCLOCK).id()));
    }

    @Test
    void shouldAttackWithAShipInSpace() {
        assertInvalid(new AttackGalacticaEvent(raider().id()));
    }
}
