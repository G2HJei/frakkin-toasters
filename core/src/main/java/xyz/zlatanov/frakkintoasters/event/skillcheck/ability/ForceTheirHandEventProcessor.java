package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ForceTheirHandEventProcessor extends EventProcessor<ForceTheirHandEvent> {

    // A card from the player's hand, or none to draw a Mutiny Card instead.
    @Override
    protected boolean isValid() {
        return event.skillCard() == null || holdsSkillCards(List.of(event.skillCard()));
    }

    @Override
    public Followup process() {
        val card = event.skillCard();
        if (card == null) {
            return single(new DrawMutinyCardEvent(player.number()));
        }
        ops.skillCheck().playCards(player.number(), card);
        return NONE;
    }
}
