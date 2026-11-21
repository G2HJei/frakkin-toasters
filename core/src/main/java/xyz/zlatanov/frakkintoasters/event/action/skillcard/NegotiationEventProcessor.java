package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.NEGOTIATION;

public class NegotiationEventProcessor extends SkillCardEventProcessor<NegotiationEvent> {

    @Override
    public SkillCardType type() {
        return NEGOTIATION;
    }

    // Placing a basestar isn't launching one, so the hold doesn't stop it. The basestar comes from the supply or the
    // Cylon Fleet board, or isn't placed (rule 11.4.2).
    @Override
    protected Followup resolve() {
        ops.holdCylonShips();
        return all(new PlaceShipsEvent(1, 0, 0), new DrawSkillCardsOfColorEvent(player.number(), POLITICS, 1));
    }
}
