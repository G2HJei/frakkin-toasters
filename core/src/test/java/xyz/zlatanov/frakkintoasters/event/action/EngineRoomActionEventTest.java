package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class EngineRoomActionEventTest extends EventTest {

    SkillCard card1 = new SkillCard(3, REPAIR);
    SkillCard card2 = new SkillCard(4, SCIENTIFIC_RESEARCH);
    SkillCard card3 = new SkillCard(2, STRATEGIC_PLANNING);

    @BeforeEach
    void setUp() {
        game.player(1).gainSkillCards(card1, card2, card3);
    }

    @Test
    void shouldDiscardTwoSkillCardsAndActivateEngineRoom() {
        val followups = new EngineRoomActionEvent(1, card1, card2).execute(game);

        assertEquals(Followup.NONE, followups);
        assertEquals(List.of(card3), player(1).skillCards().cards());
        assertTrue(galacticaBoard.engineRoomActivated());
    }

    @Test
    void shouldBeInvalidWhenPlayerDoesNotHaveCard() {
        val missingCard = new SkillCard(5, EXECUTIVE_ORDER);
        assertFalse(new EngineRoomActionEvent(1, card1, missingCard).isValid(game));
    }

    @Test
    void shouldBeInvalidWhenDiscardingSameCardTwice() {
        assertFalse(new EngineRoomActionEvent(1, card1, card1).isValid(game));
    }

}
