package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DrawMutinyCardEventProcessor extends EventProcessor<DrawMutinyCardEvent> {

    // Revealed Cylons never draw Mutiny Cards, and Cylon Leaders only while Infiltrating (rules 5.2.3.4 and 5.2.3.5).
    // Moving to the "Brig" on the second Mutiny Card is left to the engine (see PlayerOps.drawMutinyCard()).
    @Override
    public Followup process() {
        if (player.isHuman()) {
            ops.player(player.number()).drawMutinyCard();
        }
        return Followup.NONE;
    }
}
