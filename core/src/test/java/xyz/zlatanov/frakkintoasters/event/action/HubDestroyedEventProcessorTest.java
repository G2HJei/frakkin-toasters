package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HUB_DESTROYED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class HubDestroyedEventProcessorTest extends EventTestHarness<HubDestroyedEvent> {

    SkillCard card1 = new SkillCard(3, REPAIR);
    SkillCard card2 = new SkillCard(4, SCIENTIFIC_RESEARCH);
    SkillCard card3 = new SkillCard(2, STRATEGIC_PLANNING);

    @Test
    void shouldDiscard3SkillCardsDrawSuperCrisisAndMoveToCylonFleet() {
        galacticaBoard.destroyResurrectionShip();
        player(1).character(TOM_ZAREK);
        player(1).gainSkillCards(card1, card2, card3);
        moveTo(HUB_DESTROYED, TOM_ZAREK);

        execute(new HubDestroyedEvent(1, card1, card2, card3));

        assertNoSkillCards(1);
        assertEquals(1, player(1).superCrisisCards().cards().size());
        assertEquals(CYLON_FLEET, locate(TOM_ZAREK));
    }
}
