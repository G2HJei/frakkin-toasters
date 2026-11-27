package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.attack;
import static xyz.zlatanov.frakkintoasters.event.ViperActivation.move;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class ViperActivationRoundEventProcessorTest extends EventTestHarness<ViperActivationRoundEvent> {

    private Viper own;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        own = viperAt(GALACTICA_SPACE_5_OCLOCK);
        ops.player(1).pilot(own.id());
    }

    // Each attack is followed by the next round, and the player may stop instead.
    @Test
    void theAttacksOfTheRound() {
        Raider raider = raiderAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new ViperActivationRoundEvent(1, own.id(), 4, true));

        assertFollowup(one(
                all(single(new GrantedViperActivationEvent(1, attack(own.id(), raider.id()))),
                        single(new ViperActivationRoundEvent(1, own.id(), 3, true))),
                single(new NoOpEvent(1))));
    }

    @Test
    void theLastRoundQueuesNoOther() {
        Raider raider = raiderAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new ViperActivationRoundEvent(1, own.id(), 1, true));

        assertFollowup(one(single(new GrantedViperActivationEvent(1, attack(own.id(), raider.id()))),
                single(new NoOpEvent(1))));
    }

    // Nothing to attack ends the rounds.
    @Test
    void noTargetEndsTheRounds() {
        execute(new ViperActivationRoundEvent(1, own.id(), 4, true));
    }

    // An unmanned viper moves 1 space area (rule 11.1.1).
    @Test
    void anUnmannedViperMoves() {
        Viper unmanned = viperAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new ViperActivationRoundEvent(1, unmanned.id(), 1, false));

        assertFollowup(one(single(new GrantedViperActivationEvent(1, move(unmanned.id(), GALACTICA_SPACE_5_OCLOCK))),
                single(new GrantedViperActivationEvent(1, move(unmanned.id(), GALACTICA_SPACE_9_OCLOCK))),
                single(new NoOpEvent(1))));
    }

    // Never one another player is piloting (rule 11.1).
    @Test
    void notAnotherPlayersViper() {
        player(2).character(LEE_APOLLO_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        Viper theirs = viperAt(GALACTICA_SPACE_7_OCLOCK);
        ops.player(2).pilot(theirs.id());

        execute(new ViperActivationRoundEvent(1, theirs.id(), 1, false));
    }

    @Test
    void aRoundHasActivationsLeft() {
        assertInvalid(new ViperActivationRoundEvent(1, own.id(), 0, true));
    }
}
