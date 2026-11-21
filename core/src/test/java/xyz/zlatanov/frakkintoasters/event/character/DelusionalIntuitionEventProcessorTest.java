package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_1_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DELUSIONAL_INTUITION;

class DelusionalIntuitionEventProcessorTest extends EventTestHarness<DelusionalIntuitionEvent> {

    // He draws 1 Skill Card from any deck, outside his skill set too.
    @Test
    void drawsASkillCardOfAnyColor() {
        player(1).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);

        execute(new DelusionalIntuitionEvent(1));

        assertTrue(game.turn().hasUsed(1, DELUSIONAL_INTUITION));
        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_1_OF_ANY_COLOR));
    }

    @Test
    void onlyGaiusBaltar() {
        player(1).character(LAURA_ROSLIN);

        assertInvalid(new DelusionalIntuitionEvent(1));
    }
}
