package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

public class EstablishDogsvilleEventProcessor extends QuorumCardEventProcessor<EstablishDogsvilleEvent> {

    @Override
    public QuorumCard card() {
        return QuorumCard.ESTABLISH_DOGSVILLE;
    }

    @Override
    protected Followup resolve() {
        return all(gain(POPULATION, 1), lose(MORALE, 1), thenDiscard());
    }
}
