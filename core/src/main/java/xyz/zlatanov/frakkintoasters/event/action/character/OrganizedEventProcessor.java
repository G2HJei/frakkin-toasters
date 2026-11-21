package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class OrganizedEventProcessor extends CharacterActionEventProcessor<OrganizedEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.ORGANIZED;
    }

    static final int LOCATIONS = 3;

    public static boolean canUse(GameView game, PlayerView player) {
        return game.locate(player.character()).filter(Location.BRIG::equals).isEmpty()
                && !RemoteActions.locations(game, player, false).isEmpty();
    }

    @Override
    protected Followup resolve() {
        return next(game, event.playerNumber(), List.of());
    }

    /**
     * @return the choice of the next location, one he hasn't used, or nothing once he used 3 or none is left
     */
    static Followup next(GameView game, int playerNumber, List<Location> used) {
        if (used.size() >= LOCATIONS) {
            return NONE;
        }
        return one(RemoteActions.locations(game, game.player(playerNumber), false).stream()
                .filter(location -> !used.contains(location))
                .map(location -> new OrganizedLocationEvent(playerNumber, location, used))
                .toArray(Event[]::new));
    }
}
