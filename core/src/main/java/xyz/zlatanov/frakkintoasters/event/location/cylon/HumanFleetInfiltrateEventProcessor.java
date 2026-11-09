package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class HumanFleetInfiltrateEventProcessor extends LocationActionProcessor<HumanFleetInfiltrateEvent> {
    @Override
    protected boolean isValidAction() {
        val location = event.galacticaLocation();
        return player.character().type() == CYLON_LEADER
                && location != null && GALACTICA.locations().contains(location)
                && !location.isHazardousLocation();
    }

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).infiltrateGalactica(event.galacticaLocation());
        return Followup.NONE;
    }
}
