package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class DestroyPegasusEventProcessor extends EventProcessor<DestroyPegasusEvent> {

    /**
     * @return whether every location of a Pegasus still in play is damaged
     */
    public static boolean isWrecked(GameView game) {
        val pegasus = game.boards().pegasus();
        return !pegasus.destroyed() && pegasus.damagedLocations().containsAll(pegasus.locations());
    }

    @Override
    protected boolean isValid() {
        return isWrecked(game);
    }

    @Override
    public Followup process() {
        ops.pegasus().destroy();
        return NONE;
    }
}
