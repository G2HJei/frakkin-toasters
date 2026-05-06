package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;

class ActivateCylonBasestarEventTest {

    FakeDie        die            = new FakeDie();
    Game           game           = Game.builder().die(die).build();
    GalacticaBoard galacticaBoard = game.boards().galactica();

    @Test
    void shouldDoNothingOnLowRoll() {
        val basestar = game.cylonShips().basestar();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(3);

        val followup = new ActivateCylonBasestarEvent(basestar.id()).execute(game);

        assertEquals(NONE, followup);
    }

    @Test
    void shouldFollowupWithDamageDecisionOnHighRoll() {
        val basestar = game.cylonShips().basestar();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(4);

        val followup = new ActivateCylonBasestarEvent(basestar.id()).execute(game);

        assertEquals(one(new DamageGalacticaEvent(), new DamagePegasusEvent()), followup);
    }

    @Test
    void shouldNotAttackWhenBasestarHasDisabledWeapons() {
        val basestar = game.cylonShips().basestar();
        basestar.damage(DISABLED_WEAPONS);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = new ActivateCylonBasestarEvent(basestar.id()).execute(game);

        assertEquals(NONE, followup);
    }

    @Test
    void shouldStillAttackWhenBasestarHasNonDisabledWeaponsDamage() {
        val basestar = game.cylonShips().basestar();
        basestar.damage(STRUCTURAL_DAMAGE);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);
        die.nextRoll(5);

        val followup = new ActivateCylonBasestarEvent(basestar.id()).execute(game);

        assertEquals(one(new DamageGalacticaEvent(), new DamagePegasusEvent()), followup);
    }
}
