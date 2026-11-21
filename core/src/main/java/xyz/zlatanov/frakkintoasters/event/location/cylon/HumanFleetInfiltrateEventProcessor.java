package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.CharacterMoves;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class HumanFleetInfiltrateEventProcessor extends LocationActionProcessor<HumanFleetInfiltrateEvent> {
    @Override
    protected boolean isValidAction() {
        val location = event.galacticaLocation();
        // Not again while Infiltrating, e.g. through "Resolute" (errata A2.1.1.1).
        return player.character().type() == CYLON_LEADER
                && !player.isInfiltrating()
                && location != null && GALACTICA.locations().contains(location)
                && !location.isHazardousLocation()
                && !CharacterMoves.cloudedBlocks(game, player, location);
    }

    @Override
    protected Followup act() {
        ops.player(event.playerNumber()).infiltrateGalactica(event.galacticaLocation());
        return Followup.NONE;
    }
}
