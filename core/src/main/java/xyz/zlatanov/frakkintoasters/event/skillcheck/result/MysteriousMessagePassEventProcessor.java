package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

public class MysteriousMessagePassEventProcessor extends EventProcessor<MysteriousMessagePassEvent> {

    // "may": the current player searches the deck, or leaves it.
    @Override
    public Followup process() {
        val current = game.turn().currentPlayerNumber();
        return FollowupUtils.optional(current, new PlayerDecisionEvent<>(current, DiscardFromDestinyDeckEvent.class));
    }
}
