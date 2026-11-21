package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public class ResoluteEventProcessor extends CharacterActionEventProcessor<ResoluteEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.RESOLUTE;
    }

    // "Any undamaged location", the Cylon locations included.
    public static boolean canUse(GameView game, PlayerView player) {
        return !RemoteActions.locations(game, player, true).isEmpty();
    }

    @Override
    protected boolean isValidUse() {
        return event.location() != null && RemoteActions.locations(game, player, true).contains(event.location());
    }

    @Override
    protected Followup resolve() {
        return RemoteActions.actAt(ops, event.playerNumber(), event.location());
    }
}
