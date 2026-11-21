package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class PlaceInSetupLocationEventProcessor extends EventProcessor<PlaceInSetupLocationEvent> {

    @Override
    protected boolean isValid() {
        return event.location() != null && game.locate(player.character()).isEmpty()
                && startLocations(game, player.character()).contains(event.location());
    }

    /**
     * @return where the character may start: his setup locations that still exist, empty for a special setup
     */
    public static List<Location> startLocations(GameView game, Character character) {
        val setup = List.of(character.setup());
        val open = setup.stream().filter(location -> !game.isDestroyed(location)).toList();
        if (!setup.isEmpty() && open.isEmpty()) {
            // "If the character's normal starting location is unavailable (for example, as a result of the "Bomb on
            // Colonial 1" Super Crisis Card), the character starts in "Sickbay" instead." (rule 7.4)
            return List.of(SICKBAY);
        }
        return open;
    }

    // A Cylon Leader who starts on a location of the humans starts Infiltrating there: Sharon "Athena" Agathon (rule
    // 6.2.1.1), and D'Anna Biers at the "Press Room".
    static void startAt(GameOps ops, Character character, Location location) {
        if (character.type() == CYLON_LEADER && !location.isCylonLocation()) {
            ops.player(character).infiltrateGalactica(location);
        } else {
            ops.player(character).moveTo(location);
        }
    }

    @Override
    public Followup process() {
        startAt(ops, player.character(), event.location());
        return Followup.NONE;
    }
}
