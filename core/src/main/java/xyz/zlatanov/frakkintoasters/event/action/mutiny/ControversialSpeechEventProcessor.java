package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class ControversialSpeechEventProcessor extends MutinyCardEventProcessor<ControversialSpeechEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.CONTROVERSIAL_SPEECH;
    }

    // The roll decides where the card goes, so ControversialSpeechResultEvent puts it there.
    @Override
    protected Followup resolve() {
        return rollDie(new ControversialSpeechResultEvent());
    }
}
