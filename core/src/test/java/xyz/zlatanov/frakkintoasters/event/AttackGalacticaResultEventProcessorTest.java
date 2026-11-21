package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;

class AttackGalacticaResultEventProcessorTest extends EventTestHarness<AttackGalacticaResultEvent> {

    @Test
    void shouldMissWithARaiderBelow8() {
        int raiderId = raiderAt(GALACTICA_SPACE_4_OCLOCK).id();
        rolled(7);

        execute(new AttackGalacticaResultEvent(raiderId));
    }

    // The current player may draw a Pegasus damage token instead (rule 11.7.2).
    @Test
    void shouldDamageGalacticaWithARaiderOn8() {
        int raiderId = raiderAt(GALACTICA_SPACE_4_OCLOCK).id();
        rolled(8);

        execute(new AttackGalacticaResultEvent(raiderId));

        assertFollowup(single(new DamageBattlestarEvent()));
    }

    @Test
    void shouldMissWithABasestarBelow4() {
        int basestarId = basestarAt(GALACTICA_SPACE_4_OCLOCK).id();
        rolled(3);

        execute(new AttackGalacticaResultEvent(basestarId));
    }

    @Test
    void shouldDamageGalacticaWithABasestarOn4OrHigher() {
        int basestarId = basestarAt(GALACTICA_SPACE_4_OCLOCK).id();
        rolled(4);

        execute(new AttackGalacticaResultEvent(basestarId));

        assertFollowup(single(new DamageBattlestarEvent()));
    }
}
