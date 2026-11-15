package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.AttackGalacticaEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class ActivateCylonBasestarEventProcessorTest extends EventTestHarness<ActivateCylonBasestarEvent> {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        basestar = basestarAt(GALACTICA_SPACE_8_OCLOCK);
    }

    @Test
    void shouldAttackGalactica() {
        execute(new ActivateCylonBasestarEvent(basestar.id()));
        assertFollowup(new AttackGalacticaEvent(basestar.id()));
    }

    @Test
    void shouldNotAttackWhenBasestarHasDisabledWeapons() {
        basestar.damage(DISABLED_WEAPONS);
        execute(new ActivateCylonBasestarEvent(basestar.id()));
    }

    @Test
    void shouldStillAttackWhenBasestarHasNonDisabledWeaponsDamage() {
        basestar.damage(STRUCTURAL_DAMAGE);

        execute(new ActivateCylonBasestarEvent(basestar.id()));

        assertFollowup(new AttackGalacticaEvent(basestar.id()));
    }
}
