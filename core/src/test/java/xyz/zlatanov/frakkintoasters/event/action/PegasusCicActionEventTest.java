package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class PegasusCicActionEventTest {
    FakeDie die  = new FakeDie();
    Game    game = Game.builder()
            .die(die)
            .build();

    @Test
    void shouldDamagePegasus() {
        die.nextRoll(3);
        val followup = new PegasusCicActionEvent().execute(game);
        assertEquals(followWith(new DamagePegasusEvent()), followup);
    }

    @Test
    void shouldDamageGalacticaFuel() {
        die.nextRoll(6);
        val followup = new PegasusCicActionEvent().execute(game);
        assertEquals(
                followWith(
                        one(new DamagePegasusEvent(),
                                new DamageGalacticaEvent())),
                followup);
    }

    @Test
    void shouldDamageBasestarTwice() {
        die.nextRoll(8);
        val followup = new PegasusCicActionEvent().execute(game);
        assertEquals(
                followWith(
                        all(
                                new DamageBasestarEvent(),
                                new DamageBasestarEvent())),
                followup);
    }
}