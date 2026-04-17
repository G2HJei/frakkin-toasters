package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public record HumanFleetInfiltratePlayerEvent(int playerNumber, Location galacticaLocation) implements PlayerEvent {

    @Override
    public boolean isValid(Game game) {
        return player(game).character().type() == CYLON_LEADER
                && LOCATION_AREAS.get("Galactica").contains(galacticaLocation)
                && !galacticaLocation.isHazardousLocation();
    }

    @Override
    public List<Followup> apply(Game game) {
        player(game).infiltrateGalactica();
        game.moveTo(galacticaLocation, player(game).character());
        return List.of();
    }
}
