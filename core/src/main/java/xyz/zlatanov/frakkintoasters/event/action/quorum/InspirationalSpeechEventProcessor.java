package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class InspirationalSpeechEventProcessor extends QuorumCardEventProcessor<InspirationalSpeechEvent> {

    @Override
    public QuorumCard card() {
        return QuorumCard.INSPIRATIONAL_SPEECH;
    }

    @Override
    protected Followup resolve() {
        return rollDie(new InspirationalSpeechResultEvent());
    }
}
