package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUB_DESTROYED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class HubDestroyedActionEventTest extends EventTest {

    SkillCard card1 = new SkillCard(3, REPAIR);
    SkillCard card2 = new SkillCard(4, SCIENTIFIC_RESEARCH);
    SkillCard card3 = new SkillCard(2, STRATEGIC_PLANNING);

    @Test
    void shouldDiscard3SkillCardsDrawSuperCrisisAndMoveToCylonFleet() {
        galacticaBoard.destroyResurrectionShip();
        player(1).selectCharacter(TOM_ZAREK)
                .gainSkillCards(card1, card2, card3);
        game.moveTo(HUB_DESTROYED, TOM_ZAREK);

        execute(new HubDestroyedActionEvent(1, card1, card2, card3));

        assertTrue(player(1).skillCards().cards().isEmpty());
        assertEquals(1, player(1).superCrisisCards().cards().size());
        assertEquals(CYLON_FLEET, game.locate(TOM_ZAREK));
    }
}
