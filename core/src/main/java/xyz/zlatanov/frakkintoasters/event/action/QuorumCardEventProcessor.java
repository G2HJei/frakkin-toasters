package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.quorum.DiscardPlayedQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

/**
 * Plays a Quorum Card. The player has to be human and hold the card, and subclasses check the details of the event in
 * {@link #isValidPlay()}. The card leaves the hand, then {@link #resolve()} resolves it and puts it where its text
 * says, usually last: the discard pile, out of the game, or in play.
 */
public abstract class QuorumCardEventProcessor<T extends QuorumCardEvent> extends EventProcessor<T> {

    public abstract QuorumCard card();

    @Override
    protected final boolean isValid() {
        // Revealed Cylons can't use the actions of Quorum Cards (rule 6.1.2.3), and hold no title anyway.
        return player.isHuman() && holdsCard() && isValidPlay();
    }

    protected boolean isValidPlay() {
        return true;
    }

    @Override
    public final Followup process() {
        ops.titles().play(card());
        return resolve();
    }

    protected abstract Followup resolve();

    // "Then discard this card", after the effects the card queued.
    protected Event thenDiscard() {
        return new DiscardPlayedQuorumCardEvent(card());
    }

    // Only the President plays from the Quorum hand (rule 4.4.1.1), also from the "Brig" (rule 4.4.1.2).
    private boolean holdsCard() {
        return player.character() != null
                && player.character() == game.president()
                && game.presidentHand().cards().contains(card());
    }
}
