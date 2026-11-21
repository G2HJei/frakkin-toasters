package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FORWARD_THINKER;

public class ForwardThinkerEventProcessor extends EventProcessor<ForwardThinkerEvent> {

    // He takes the action of the location he is at, so not as a pilot, nor at a location without an action for him.
    public static boolean canUse(GameView game, PlayerView player) {
        return player.hasAbility(FORWARD_THINKER) && LocationEvent.canAct(game, player);
    }

    /**
     * @return the choice to activate his location or not, {@link Followup#NONE} when he can't
     */
    public static Followup offer(GameView game, PlayerView player) {
        return canUse(game, player)
                ? one(new ForwardThinkerEvent(player.number()), new NoOpEvent(player.number()))
                : NONE;
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        int lee = player.number();
        ops.player(lee).useAbility(FORWARD_THINKER);
        val locationAction = new PlayerDecisionEvent<>(lee, LocationEvent.class);
        if (game.turn().actingPlayerNumber() == lee) {
            return single(locationAction);
        }
        ops.actingPlayer(lee); // he acts out of his turn, e.g. after an "Executive Order" granted by "State of Emergency"
        return all(locationAction, new ResetActingPlayerEvent());
    }
}
