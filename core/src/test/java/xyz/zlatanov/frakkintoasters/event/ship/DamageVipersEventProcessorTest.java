package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_5_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_7_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class DamageVipersEventProcessorTest extends EventTestHarness<DamageVipersEvent> {

    // One at a time: the viper that is chosen, then the rest of the effect.
    @Test
    void shouldDamageOneViperAtATime() {
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new DamageVipersEvent(2, SPACE));

        assertFollowup(all(new DamageChosenViperEvent(chooser(), viper.id()), new DamageVipersEvent(1, SPACE)));
    }

    // Vipers of the same kind in the same place are alike: only the first is an option. A piloted viper, a viper in
    // another area and an assault raptor, which counts as a viper (rule 11.3.2.2), are each an option of their own.
    @Test
    void shouldOfferOneFighterOfEachKind() {
        player(1).character(KARA_STARBUCK_THRACE);
        val piloted = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.player(1).pilot(piloted.id());
        val unmanned = viperAt(GALACTICA_SPACE_5_OCLOCK);
        val alike = viperAt(GALACTICA_SPACE_5_OCLOCK);
        val elsewhere = viperAt(GALACTICA_SPACE_7_OCLOCK);
        val assaultRaptor = assaultRaptorAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new DamageVipersEvent(1, SPACE));

        assertFollowup(all(
                one(new DamageChosenViperEvent(chooser(), piloted.id()),
                        new DamageChosenViperEvent(chooser(), Math.min(unmanned.id(), alike.id())),
                        new DamageChosenViperEvent(chooser(), elsewhere.id()),
                        new DamageChosenViperEvent(chooser(), assaultRaptor.id())),
                single(new DamageVipersEvent(0, SPACE))));
    }

    // The player taking an action chooses (rule 11.3.2.2).
    @Test
    void shouldLetTheActingPlayerChoose() {
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.actingPlayer(2);

        execute(new DamageVipersEvent(1, SPACE));

        assertFollowup(all(new DamageChosenViperEvent(2, viper.id()), new DamageVipersEvent(0, SPACE)));
    }

    // "Damage 2 vipers in space areas (if able)."
    @Test
    void shouldStopWhenNoViperIsLeft() {
        execute(new DamageVipersEvent(2, SPACE));
    }

    @Test
    void shouldStopWhenAllAreDamaged() {
        viperAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new DamageVipersEvent(0, SPACE));
    }

    private int chooser() {
        return game.actingPlayer().number();
    }
}
