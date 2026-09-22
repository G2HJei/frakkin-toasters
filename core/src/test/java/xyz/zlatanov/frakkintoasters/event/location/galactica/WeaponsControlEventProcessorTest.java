package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.AttackHeavyRaiderEvent;
import xyz.zlatanov.frakkintoasters.event.AttackRaiderEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class WeaponsControlEventProcessorTest extends EventTestHarness<WeaponsControlEvent> {

    @Test
    void shouldFollowupWithAttackRaiderEvent() {
        val raider = raider();
        val raiderId = raider.id();
        place(GALACTICA_SPACE_2_OCLOCK, raider);

        execute(new WeaponsControlEvent(1, raiderId));

        assertFollowup(new AttackRaiderEvent(raiderId));
    }

    @Test
    void shouldFollowupWithAttackHeavyRaiderEvent() {
        val hRaider = heavyRaider();
        val hRaiderId = hRaider.id();
        place(GALACTICA_SPACE_4_OCLOCK, hRaider);

        execute(new WeaponsControlEvent(1, hRaiderId));

        assertFollowup(new AttackHeavyRaiderEvent(hRaiderId));
    }

    @Test
    void shouldFollowupWithAttackBasestarEvent() {
        val basestar = basestar();
        val basestarId = basestar.id();
        place(GALACTICA_SPACE_6_OCLOCK, basestar);

        execute(new WeaponsControlEvent(1, basestarId));

        assertFollowup(new AttackBasestarEvent(GALACTICA, basestarId));
    }
}