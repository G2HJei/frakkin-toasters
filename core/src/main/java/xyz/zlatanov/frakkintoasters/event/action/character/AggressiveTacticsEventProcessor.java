package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.AGGRESSIVE_TACTICS;

public class AggressiveTacticsEventProcessor extends CharacterMovementEventProcessor<AggressiveTacticsEvent> {

    // Offered only when he has an action to take, so the miracle isn't spent on nothing.
    public static boolean canUse(GameView game, PlayerView player) {
        return Actions.hasAnAction(game, player);
    }

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.AGGRESSIVE_TACTICS;
    }

    @Override
    protected Followup resolve() {
        // Each grant offers his actions as the game stands then, so the second sees what the first changed.
        return all(
                new GrantEvent(player.number(), ACTION, AGGRESSIVE_TACTICS),
                new GrantEvent(player.number(), ACTION, AGGRESSIVE_TACTICS));
    }
}
