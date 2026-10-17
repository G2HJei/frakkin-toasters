package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ForceTheirHandEventProcessor extends EventProcessor<ForceTheirHandEvent> {

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
