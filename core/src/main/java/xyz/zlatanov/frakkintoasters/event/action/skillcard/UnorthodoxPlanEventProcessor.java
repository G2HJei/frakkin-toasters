package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ArmoryEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommandEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommunicationsEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.WeaponsControlEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.ANYWHERE_EVEN_IF_DAMAGED;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.UNORTHODOX_PLAN;

public class UnorthodoxPlanEventProcessor extends SkillCardEventProcessor<UnorthodoxPlanEvent> {

    private static final List<Location> LOCATIONS = List.of(COMMAND, ARMORY, WEAPONS_CONTROL, COMMUNICATIONS);

    public static boolean canPlay(GameView game, PlayerView player) {
        return LOCATIONS.stream().anyMatch(location -> canTakeActionOf(game, player, location));
    }

    // The location's own rules, damaged or not, and something for its action to do: a viper for "Command", a civilian
    // ship for "Communications". The player doesn't have to be there: the card doesn't ask it.
    private static boolean canTakeActionOf(GameView game, PlayerView player, Location location) {
        if (location == null || !LOCATIONS.contains(location) || !LocationEvent.canActIgnoringDamage(game, player, location)) {
            return false;
        }
        return switch (location) {
            case COMMAND -> canActivateUnmanned(game);
            case COMMUNICATIONS -> !game.boards().galactica().shipsInSpace(CivilianShip.class).isEmpty();
            default -> true;
        };
    }

    @Override
    public SkillCardType type() {
        return UNORTHODOX_PLAN;
    }

    @Override
    protected boolean isValidPlay() {
        return canTakeActionOf(game, player, event.location());
    }

    @Override
    protected Followup resolve() {
        int n = player.number();
        return single(switch (event.location()) {
            case COMMAND -> new PlayerDecisionEvent<>(n, CommandEvent.class, ANYWHERE_EVEN_IF_DAMAGED);
            case ARMORY -> new PlayerDecisionEvent<>(n, ArmoryEvent.class, ANYWHERE_EVEN_IF_DAMAGED);
            case WEAPONS_CONTROL -> new PlayerDecisionEvent<>(n, WeaponsControlEvent.class, ANYWHERE_EVEN_IF_DAMAGED);
            default -> new PlayerDecisionEvent<>(n, CommunicationsEvent.class, ANYWHERE_EVEN_IF_DAMAGED);
        });
    }
}
