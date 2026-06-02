package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class ActivateCylonBasestarEventProcessorTest extends EventTestHarness<ActivateCylonBasestarEvent> {

    Basestar                   basestar;
    ActivateCylonBasestarEvent event;

    @BeforeEach
    void setUp() {
        basestar = basestarAt(GALACTICA_SPACE_8_OCLOCK);
        event = new ActivateCylonBasestarEvent(basestar.id());
    }

    @Test
    void shouldDoNothingOnLowRoll() {
        nextRoll(3);
        executeAndAssertNoFollowup(event);
    }

    @Test
    void shouldFollowupWithDamageDecisionOnHighRoll() {
        nextRoll(4);
        executeAndAssertFollowup(event, one(new DamageGalacticaEvent(), new DamagePegasusEvent()));
    }

    @Test
    void shouldNotAttackWhenBasestarHasDisabledWeapons() {
        basestar.damage(DISABLED_WEAPONS);
        executeAndAssertNoFollowup(event);
    }

    @Test
    void shouldStillAttackWhenBasestarHasNonDisabledWeaponsDamage() {
        basestar.damage(STRUCTURAL_DAMAGE);
        nextRoll(5);
        executeAndAssertFollowup(event, one(new DamageGalacticaEvent(), new DamagePegasusEvent()));
    }
}
