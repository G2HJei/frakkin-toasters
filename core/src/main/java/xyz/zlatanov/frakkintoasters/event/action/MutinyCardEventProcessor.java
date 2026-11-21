package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.DiscardPlayedMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.RemoveMutinyCardFromGameEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

/**
 * Plays a Mutiny Card. The player has to be human, an Infiltrating Cylon Leader included (rules 5.2.3.4 and 5.2.3.5), and
 * hold the card, also in the "Brig" (rule 5.2.3.2). Subclasses check the details of the event in
 * {@link #isValidPlay()}. The card leaves the hand, then {@link #resolve()} resolves it and puts it where its text says:
 * the discard pile, or out of the game.
 */
public abstract class MutinyCardEventProcessor<T extends MutinyCardEvent> extends EventProcessor<T> {

    public abstract MutinyCard card();

    @Override
    protected final boolean isValid() {
        return player.isHuman() && player.mutinyCards().cards().contains(card()) && isValidPlay();
    }

    protected boolean isValidPlay() {
        return true;
    }

    @Override
    public final Followup process() {
        ops.player(player.number()).playMutinyCard(card());
        return resolve();
    }

    protected abstract Followup resolve();

    // "Then discard this card", after the effects the card queued before it.
    protected Event thenDiscard() {
        return new DiscardPlayedMutinyCardEvent(card());
    }

    // "Remove this card from the game", after the effects the card queued before it.
    protected Event thenRemove() {
        return new RemoveMutinyCardFromGameEvent(card());
    }
}
