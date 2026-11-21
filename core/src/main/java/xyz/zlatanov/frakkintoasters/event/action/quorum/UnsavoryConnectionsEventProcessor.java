package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

public class UnsavoryConnectionsEventProcessor extends QuorumCardEventProcessor<UnsavoryConnectionsEvent> {

    @Override
    public QuorumCard card() {
        return QuorumCard.UNSAVORY_CONNECTIONS;
    }

    @Override
    protected boolean isValidPlay() {
        return event.resource() == FOOD || event.resource() == FUEL;
    }

    // With fewer cards in the hand he discards what he has, and with fewer Treachery Cards left he draws what is left.
    @Override
    protected Followup resolve() {
        return all(new DiscardRandomSkillCardsEvent(player.number(), 2), treachery(player.number(), 2),
                gain(event.resource(), 1), thenDiscard());
    }
}
