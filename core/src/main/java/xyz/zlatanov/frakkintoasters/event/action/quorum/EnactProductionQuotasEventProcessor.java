package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

public class EnactProductionQuotasEventProcessor extends QuorumCardEventProcessor<EnactProductionQuotasEvent> {

    @Override
    public QuorumCard card() {
        return QuorumCard.ENACT_PRODUCTION_QUOTAS;
    }

    @Override
    protected Followup resolve() {
        return all(gain(FOOD, 1), lose(MORALE, 1), thenDiscard());
    }
}
