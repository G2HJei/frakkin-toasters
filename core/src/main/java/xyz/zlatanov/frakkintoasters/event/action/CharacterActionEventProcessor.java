package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;

/**
 * Uses an "Action:" ability of the player's character sheet. The ability has to be in effect (a revealed Cylon's are
 * ignored, a Cylon Leader's always apply), a miracle ability needs its miracle tokens, and subclasses check the details
 * of the event in {@link #isValidUse()}. The use is recorded, which spends the miracle tokens, then {@link #resolve()}
 * resolves it.
 */
public abstract class CharacterActionEventProcessor<T extends CharacterActionEvent> extends EventProcessor<T> {

    public abstract CharacterAbility ability();

    @Override
    protected final boolean isValid() {
        return CharacterActions.canUse(game, player, ability()) && isValidUse();
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
