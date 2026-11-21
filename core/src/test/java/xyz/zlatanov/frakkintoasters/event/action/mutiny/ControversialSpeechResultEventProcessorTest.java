package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.CONTROVERSIAL_SPEECH;

class ControversialSpeechResultEventProcessorTest extends EventTestHarness<ControversialSpeechResultEvent> {

    @Test
    void onSixMoraleAndTheCardLeavesTheGame() {
        rolled(6);

        execute(new ControversialSpeechResultEvent());

        assertFollowup(all(gain(MORALE, 1), new RemoveMutinyCardFromGameEvent(CONTROVERSIAL_SPEECH)));
    }

    @Test
    void lowerEveryPlayerDrawsATreacheryCard() {
        rolled(5);

        execute(new ControversialSpeechResultEvent());

        assertFollowup(all(new DiscardPlayedMutinyCardEvent(CONTROVERSIAL_SPEECH), treachery(1, 1), treachery(2, 1),
                treachery(3, 1)));
    }
}
