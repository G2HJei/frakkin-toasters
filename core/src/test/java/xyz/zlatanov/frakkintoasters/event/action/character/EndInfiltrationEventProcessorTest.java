package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;

class EndInfiltrationEventProcessorTest extends EventTestHarness<EndInfiltrationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(DANNA_BIERS);
        moveTo(HUMAN_FLEET, DANNA_BIERS);
    }

    @Test
    void endsTheInfiltrationAtTheResurrectionShip() {
        ops.player(1).infiltrateGalactica(COMMAND);

        execute(new EndInfiltrationEvent(1));

        assertFalse(player(1).isInfiltrating());
        assertEquals(RESURRECTION_SHIP, locate(DANNA_BIERS));
    }

    @Test
    void nothingWhenItEndedAlready() {
        execute(new EndInfiltrationEvent(1));

        assertEquals(HUMAN_FLEET, locate(DANNA_BIERS));
    }

    // "Visions" from the "Brig" ends the Infiltration as the return action does: down to 3 Skill Cards (rule 6.2.3.5).
    @Test
    void discardsDownToThreeFromTheBrig() {
        ops.player(1).infiltrateGalactica(COMMAND);
        moveTo(BRIG, DANNA_BIERS);
        player(1).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER), new SkillCard(2, CONSOLIDATE_POWER),
                new SkillCard(3, REPAIR), new SkillCard(4, REPAIR));

        execute(new EndInfiltrationEvent(1));

        assertEquals(RESURRECTION_SHIP, locate(DANNA_BIERS));
        assertFollowup(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_3));
    }
}
