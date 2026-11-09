package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;
import static xyz.zlatanov.frakkintoasters.state.view.GalacticaBoardView.VIPER_LAUNCH_SPACES;

public class LaunchViperEventProcessor extends EventProcessor<LaunchViperEvent> {

    private static final Set<ShipType> VALID_LAUNCH_VIPER_SHIP_TYPES = Set.of(VIPER, VIPER_MARK_VII, ASSAULT_RAPTOR);

    @Override
    protected boolean isValid() {
        return event.pilot() == null ? validUnmannedLaunch() : validPilotedLaunch();
    }

    @Override
    public Followup process() {
        val humanFighterId = ops.galactica().launchHumanFighter(event.shipType(), event.location(), event.unmannedViperId());
        if (event.pilot() != null) {
            ops.player(event.pilot()).pilot(humanFighterId);
        }
        return Followup.NONE;
    }

    // A viper (a mark VII or an assault raptor too) from the "Reserves", into a space area with a viper launch icon.
    public static boolean canLaunchFromReserves(GameView game, ShipType shipType, Location location) {
        return isLaunch(shipType, location) && game.boards().galactica()
                .reserves()
                .stream()
                .anyMatch(s -> s.getClass() == shipType.shipClass());
    }

    private static boolean isLaunch(ShipType shipType, Location location) {
        return shipType != null && location != null
                && VALID_LAUNCH_VIPER_SHIP_TYPES.contains(shipType)
                && VIPER_LAUNCH_SPACES.contains(location);
    }

    private boolean validUnmannedLaunch() {
        return !event.mustLaunchInViper()
                && event.unmannedViperId() == null
                && canLaunchFromReserves(game, event.shipType(), event.location());
    }

    // The player pilots the viper himself. If all vipers are in play, he may take an unmanned one from a space area
    // instead (rule 11.2).
    private boolean validPilotedLaunch() {
        val ownPilot = event.pilot() == player.character() && player.character().canPilot();
        val viperAvailable = event.unmannedViperId() == null
                ? canLaunchFromReserves(game, event.shipType(), event.location())
                : isLaunch(event.shipType(), event.location())
                && noHumanFighterInReserves()
                && unmannedInSpace(event.unmannedViperId());
        return ownPilot && viperAvailable;
    }

    private boolean noHumanFighterInReserves() {
        return galacticaBoard.reserves().stream().noneMatch(HumanFighterView.class::isInstance);
    }

    private boolean unmannedInSpace(int humanFighterId) {
        return galacticaBoard.shipsInSpace(HumanFighterView.class)
                .stream()
                .anyMatch(s -> s.id() == humanFighterId && s.pilot() == null
                        && s.getClass() == event.shipType().shipClass());
    }
}
