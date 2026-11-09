package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

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
            ops.player(player.number()).drawMutinyCard();
        } else {
            ops.skillCheck().playCards(player.number(), card);
        }
        return Followup.NONE;
    }
}
