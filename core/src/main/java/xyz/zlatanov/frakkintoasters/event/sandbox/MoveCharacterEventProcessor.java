package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;

public class MoveCharacterEventProcessor extends EventProcessor<MoveCharacterEvent> {

    @Override
    protected boolean isValid() {
        return event.character() != null && event.destination() != null
                && game.players().stream().anyMatch(player -> player.character() == event.character())
                && !LocationsArea.GALACTICA_SPACE.locations().contains(event.destination())
                && !LocationsArea.CYLON_FLEET_SPACE.locations().contains(event.destination());
    }

    @Override
    public Followup process() {
        ops.sandbox().moveCharacter(event.character(), event.destination());
        return Followup.NONE;
    }
}
