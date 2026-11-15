package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;

// "The current player draws a Mutiny Card and 2 Skill Cards (they may be from outside his skill set)." The Mutiny Card is
// drawn by a separate event.
class ConsultTheHybridPassEventProcessorTest extends EventTestHarness<ConsultTheHybridPassEvent> {

    @Test
    void shouldLetTheCurrentPlayerDrawTwoCardsOfAnyColor() {
        execute(new ConsultTheHybridPassEvent());

        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
    }
}
