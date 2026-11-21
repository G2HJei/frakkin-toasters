package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.CONTROVERSIAL_SPEECH;

public class ControversialSpeechResultEventProcessor extends EventProcessor<ControversialSpeechResultEvent> {

    static final int LOWEST_ROLL_TO_GAIN = 6;

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null;
    }

    @Override
    public Followup process() {
        if (game.turn().dieRoll().result() >= LOWEST_ROLL_TO_GAIN) {
            return all(gain(MORALE, 1), new RemoveMutinyCardFromGameEvent(CONTROVERSIAL_SPEECH));
        }
        val effects = new ArrayList<Event>();
        effects.add(new DiscardPlayedMutinyCardEvent(CONTROVERSIAL_SPEECH));
        // every player, the Cylons too, clockwise from the current player
        playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .map(PlayerView::number)
                .forEach(n -> effects.add(treachery(n, 1)));
        return all(effects.toArray(Event[]::new));
    }
}
