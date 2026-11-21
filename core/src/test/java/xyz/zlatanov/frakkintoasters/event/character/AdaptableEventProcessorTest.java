package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TORY_FOSTER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ADAPTABLE;

class AdaptableEventProcessorTest extends EventTestHarness<AdaptableEvent> {

    // After any player's Quorum Card: 2 Skill Cards of any types.
    @Test
    void toryDrawsTwoOfAnyColor() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(TORY_FOSTER);
        assertTrue(AdaptableEventProcessor.mayBeOffered(game));
        assertEquals(one(new AdaptableEvent(2), new NoOpEvent(2)), AdaptableEventProcessor.offer(game));

        execute(new AdaptableEvent(2));

        assertTrue(game.turn().hasUsed(2, ADAPTABLE));
        assertFollowup(new PlayerDecisionEvent<>(2, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
    }

    @Test
    void notWithoutTory() {
        player(1).character(LAURA_ROSLIN);

        assertFalse(AdaptableEventProcessor.mayBeOffered(game));
        assertEquals(NONE, AdaptableEventProcessor.offer(game));
        assertInvalid(new AdaptableEvent(1));
    }
}
