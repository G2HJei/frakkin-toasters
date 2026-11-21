package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.action.quorum.FoodRationingResultEventProcessor.gainOnSix;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.INSPIRATIONAL_SPEECH;

public class InspirationalSpeechResultEventProcessor extends EventProcessor<InspirationalSpeechResultEvent> {

    @Override
    public Followup process() {
        return gainOnSix(game, ops, INSPIRATIONAL_SPEECH, MORALE);
    }
}
