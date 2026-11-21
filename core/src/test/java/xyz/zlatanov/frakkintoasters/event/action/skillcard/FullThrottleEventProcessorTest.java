package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.FULL_THROTTLE;

class FullThrottleEventProcessorTest extends EventTestHarness<FullThrottleEvent> {

    private static final SkillCard FULL_THROTTLE_1 = new SkillCard(1, FULL_THROTTLE);

    private Viper own;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(FULL_THROTTLE_1);
        own = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(1).pilot(own.id());
    }

    // Any space area, then 1 attack there if the player wants.
    @Test
    void theViperMovesAnyDistanceThenMayAttack() {
        execute(new FullThrottleEvent(1, FULL_THROTTLE_1, GALACTICA_SPACE_10_OCLOCK));

        assertEquals(GALACTICA_SPACE_10_OCLOCK, locate(own));
        assertFollowup(new ViperActivationRoundEvent(1, own.id(), 1, true));
    }

    // "Even if you didn't move."
    @Test
    void theViperMayStay() {
        execute(new FullThrottleEvent(1, FULL_THROTTLE_1, null));

        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(own));
        assertFollowup(new ViperActivationRoundEvent(1, own.id(), 1, true));
    }

    @Test
    void notToItsOwnArea() {
        assertInvalid(new FullThrottleEvent(1, FULL_THROTTLE_1, GALACTICA_SPACE_4_OCLOCK));
    }

    @Test
    void onlyIntoSpace() {
        assertInvalid(new FullThrottleEvent(1, FULL_THROTTLE_1, COMMAND));
    }
}
