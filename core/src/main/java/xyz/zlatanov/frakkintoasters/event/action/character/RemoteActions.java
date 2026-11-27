package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

/**
 * The location actions an ability lets a player take from wherever he is: Louis Hoshi's "Organized" and Sharon
 * "Athena" Agathon's "Resolute". The turn allows that one location ({@code TurnView.remoteActionLocation}), the
 * location's own decision asks for the action, and {@link EndRemoteActionEvent} ends it.
 */
@NoArgsConstructor(access = PRIVATE)
public final class RemoteActions {

    /**
     * @return the locations whose action the player could take there now: undamaged, with something for the action to
     * do. Not the "Brig", whose action is to leave it
     */
    public static List<Location> locations(GameView game, PlayerView player, boolean withCylonLocations) {
        return Arrays.stream(Location.values())
                .filter(location -> !location.isSpaceLocation() && location != BRIG)
                .filter(location -> withCylonLocations || !location.isCylonLocation())
                .filter(location -> LocationEvent.canAct(game, player, location))
                .toList();
    }

    public static Followup actAt(GameOps ops, int playerNumber, Location location) {
        ops.allowRemoteAction(playerNumber, location);
        return all(new PlayerDecisionEvent<>(playerNumber, LocationEvent.class), new EndRemoteActionEvent(playerNumber));
    }
}
