package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class HumanFleetInfiltrateEventProcessor extends EventProcessor<HumanFleetInfiltrateEvent> {
    @Override
    public boolean isValid() {
        val location = event.galacticaLocation();
        return player().character().type() == CYLON_LEADER
                && LOCATION_AREAS.get("Galactica").contains(location)
                && !location.isHazardousLocation();
    }

    @Override
    public Followup process() {
        player().infiltrateGalactica();
        game.moveTo(event.galacticaLocation(), player().character());
        return Followup.NONE;
    }
}
