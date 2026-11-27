package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;

public abstract class CharacterMovementEventProcessor<T extends CharacterMovementEvent> extends EventProcessor<T> {

    public abstract CharacterAbility ability();

    @Override
    protected final boolean isValid() {
        return CharacterMovements.canUse(game, player, ability()) && isValidUse();
    }

    protected boolean isValidUse() {
        return true;
    }

    @Override
    public final Followup process() {
        ops.player(player.number()).useAbility(ability());
        return resolve();
    }

    protected abstract Followup resolve();
}
