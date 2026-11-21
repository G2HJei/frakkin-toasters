package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

class DamageBattlestarEventProcessorTest extends EventTestHarness<DamageBattlestarEvent> {

    // The current player may draw a Pegasus damage token instead (rule 11.7.2).
    @Test
    void shouldOfferPegasusWhileItIsInPlay() {
        execute(new DamageBattlestarEvent());

        assertFollowup(one(new DamageGalacticaEvent(), new DamagePegasusEvent()));
    }

    @Test
    void shouldDamageGalacticaOncePegasusIsDestroyed() {
        pegasusBoard.destroy();

        execute(new DamageBattlestarEvent());

        assertFollowup(new DamageGalacticaEvent());
    }
}
