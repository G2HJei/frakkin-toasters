package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM;

public class SetTheAgendaLookEventProcessor extends EventProcessor<SetTheAgendaLookEvent> {

    // Only he learns the hand, so it is a CardsSeen.
    @Override
    public Followup process() {
        val hand = game.presidentHand().cards();
        if (hand.isEmpty()) {
            return NONE;
        }
        ops.player(event.playerNumber()).see(QUORUM, hand);
        return single(new PlayerDecisionEvent<>(event.playerNumber(), SetTheAgendaChoiceEvent.class));
    }
}
