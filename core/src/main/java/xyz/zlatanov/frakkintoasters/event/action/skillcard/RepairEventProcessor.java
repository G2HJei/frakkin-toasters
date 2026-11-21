package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.MaintenanceEngineerEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

public class RepairEventProcessor extends SkillCardEventProcessor<RepairEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        val location = game.locate(player.character());
        return location.filter(l -> isDamaged(game, l)).isPresent()
                || location.filter(HANGAR_DECK::equals).isPresent() && !game.boards().galactica().damagedShips().isEmpty();
    }

    @Override
    public SkillCardType type() {
        return REPAIR;
    }

    @Override
    protected boolean isValidPlay() {
        val viperIds = event.viperIds();
        if (viperIds == null) {
            return false;
        }
        val location = game.locate(player.character());
        if (viperIds.isEmpty()) {
            return location.filter(l -> isDamaged(game, l)).isPresent();
        }
        return location.filter(HANGAR_DECK::equals).isPresent() && areDamagedVipers(game, viperIds);
    }

    /**
     * @return whether the ids name up to 2 different damaged vipers, e.g. for "Resources for Galactica"
     */
    public static boolean areDamagedVipers(GameView game, List<Integer> viperIds) {
        val damaged = game.boards().galactica().damagedShips().stream().map(Ship::id).collect(toSet());
        return viperIds.size() <= 2
                && viperIds.stream().allMatch(id -> id != null && damaged.contains(id))
                && new HashSet<>(viperIds).size() == viperIds.size();
    }

    // A repaired viper goes back to the "Reserves".
    @Override
    protected Followup resolve() {
        if (event.viperIds().isEmpty()) {
            repairLocation(game, ops, game.locate(player.character()).orElseThrow());
        } else {
            event.viperIds().stream().filter(Objects::nonNull).forEach(id -> ops.ship(id).repair());
        }
        return MaintenanceEngineerEventProcessor.offer(game, player); // "Chief" Galen Tyrol's another action
    }

    public static boolean isDamaged(GameView game, Location location) {
        // A destroyed Pegasus is out of the game, damage tokens and all (rule 11.7.2.4).
        return game.boards().galactica().isDamaged(location)
                || game.boards().pegasus().isDamaged(location) && !game.boards().pegasus().destroyed();
    }

    /**
     * Repairs a damaged location of Galactica or of Pegasus.
     */
    public static void repairLocation(GameView game, GameOps ops, Location location) {
        if (game.boards().galactica().locations().contains(location)) {
            ops.galactica().repairLocation(location);
        } else {
            ops.pegasus().repairLocation(location);
        }
    }
}
