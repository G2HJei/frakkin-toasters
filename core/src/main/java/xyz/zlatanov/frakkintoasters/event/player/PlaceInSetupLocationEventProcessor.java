package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

public class PlaceInSetupLocationEventProcessor extends EventProcessor<PlaceInSetupLocationEvent> {

    @Override
    protected boolean isValid() {
        return game.locate(player.character()).isEmpty()
                && List.of(player.character().setup()).contains(event.location());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).moveTo(event.location());
        return Followup.NONE;
    }
}
