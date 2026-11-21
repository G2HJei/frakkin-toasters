package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.quorum.DiscardPlayedQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.character.AdaptableEventProcessor;
import xyz.zlatanov.frakkintoasters.event.character.OfferAdaptableEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

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
        if (player.quorumCards().cards().contains(card())) {
            ops.player(player.number()).playHeldQuorumCard(card());
        } else {
            ops.titles().play(card());
        }
        // Tory Foster's "Adaptable" answers any player's Quorum Card, after its action.
        val adaptable = AdaptableEventProcessor.mayBeOffered(game) ? single(new OfferAdaptableEvent()) : NONE;
        return allOf(resolve(), adaptable);
    }

    protected abstract Followup resolve();

    // "Then discard this card", after the effects the card queued.
    protected Event thenDiscard() {
        return new DiscardPlayedQuorumCardEvent(card());
    }

    // The card the player holds himself ("Popular Influence"), otherwise one from the President's hand, which only the
    // President plays (rule 4.4.1.1), also from the "Brig" (rule 4.4.1.2).
    private boolean holdsCard() {
        return player.quorumCards().cards().contains(card())
                || player.character() != null
                && player.character() == game.president()
                && game.presidentHand().cards().contains(card());
    }
}
