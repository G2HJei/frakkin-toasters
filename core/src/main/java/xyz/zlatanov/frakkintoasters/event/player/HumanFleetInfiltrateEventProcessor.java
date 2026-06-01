package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class HumanFleetInfiltrateEventProcessor extends EventProcessor<HumanFleetInfiltrateEvent> {
    @Override
    public boolean isValid() {
        val location = event.galacticaLocation();
        return player().character().type() == CYLON_LEADER
                && GALACTICA.locations().contains(location)
                && !location.isHazardousLocation();
    }

    @Override
    public Followup process() {
        player().infiltrateGalactica();
        game.moveTo(event.galacticaLocation(), player().character());
        return Followup.NONE;
    }
}
