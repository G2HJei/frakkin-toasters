package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.NEGOTIATION;

class NegotiationEventProcessorTest extends EventTestHarness<NegotiationEvent> {

    private static final SkillCard NEGOTIATION_5 = new SkillCard(5, NEGOTIATION);

    @Test
    void aBasestarAPoliticsCardAndTheShipsHeld() {
        player(1).character(LAURA_ROSLIN);
        player(1).gainSkillCards(NEGOTIATION_5);

        execute(new NegotiationEvent(1, NEGOTIATION_5));

        assertFollowup(all(new PlaceShipsEvent(1, 0, 0), new DrawSkillCardsOfColorEvent(1, POLITICS, 1)));
        assertTrue(game.turn().cylonShipsHeld());
    }
}
