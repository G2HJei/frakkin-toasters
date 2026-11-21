package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

public class BetrayalOfTrustEventProcessor extends MutinyCardEventProcessor<BetrayalOfTrustEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.BETRAYAL_OF_TRUST;
    }

    @Override
    protected Followup resolve() {
        return all(treachery(event.playerNumber(), 2),
                new LookAtTopCardEvent(event.playerNumber(), DESTINATION),
                thenDiscard());
    }
}
