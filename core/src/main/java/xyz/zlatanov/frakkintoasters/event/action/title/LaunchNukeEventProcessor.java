package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class LaunchNukeEventProcessor extends EventProcessor<LaunchNukeEvent> {

    // A space area without ships is a legal target too: the nuke then has no effect (rule 11.6.1).
    public static boolean canAct(GameView game, PlayerView player) {
        return player.isHuman()
                && player.character() != null
                && player.character() == game.admiral()
                && game.nukes() > 0;
    }

    @Override
    protected boolean isValid() {
        return canAct(game, player) && isSpaceArea(event.area());
    }

    @Override
    public Followup process() {
        ops.discardNuke();
        return rollDie(new NukeResultEvent(event.playerNumber(), event.area()));
    }

    public static boolean isSpaceArea(Location area) {
        return area != null && LocationsArea.GALACTICA_SPACE.locations().contains(area);
    }
}
