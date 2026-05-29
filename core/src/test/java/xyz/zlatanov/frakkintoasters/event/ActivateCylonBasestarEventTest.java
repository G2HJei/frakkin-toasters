package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class ActivateCylonBasestarEventTest extends EventTest {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        basestar = basestar();
    }

    @Test
    void shouldDoNothingOnLowRoll() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(3);

        val followup = execute(new ActivateCylonBasestarEvent(basestar.id()));

        assertEquals(NONE, followup);
    }

    @Test
    void shouldFollowupWithDamageDecisionOnHighRoll() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(4);

        val followup = execute(new ActivateCylonBasestarEvent(basestar.id()));

        assertEquals(one(new DamageGalacticaEvent(), new DamagePegasusEvent()), followup);
    }

    @Test
    void shouldNotAttackWhenBasestarHasDisabledWeapons() {
        basestar.damage(DISABLED_WEAPONS);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = execute(new ActivateCylonBasestarEvent(basestar.id()));

        assertEquals(NONE, followup);
    }

    @Test
    void shouldStillAttackWhenBasestarHasNonDisabledWeaponsDamage() {
        basestar.damage(STRUCTURAL_DAMAGE);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(5);

        val followup = execute(new ActivateCylonBasestarEvent(basestar.id()));

        assertEquals(one(new DamageGalacticaEvent(), new DamagePegasusEvent()), followup);
    }
}
