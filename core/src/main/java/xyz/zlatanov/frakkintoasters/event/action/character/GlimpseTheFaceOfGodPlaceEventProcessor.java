package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static java.lang.Math.min;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.action.character.GlimpseTheFaceOfGodEventProcessor.CARDS;

public class GlimpseTheFaceOfGodPlaceEventProcessor extends EventProcessor<GlimpseTheFaceOfGodPlaceEvent> {

    // He just took 2, so he has 2 to put back.
    @Override
    protected boolean isValid() {
        return event.cards() != null
                && event.cards().size() == min(CARDS, player.skillCards().size())
                && holdsSkillCards(event.cards());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).placeOnDestinyDeck(event.cards());
        return NONE;
    }
}
