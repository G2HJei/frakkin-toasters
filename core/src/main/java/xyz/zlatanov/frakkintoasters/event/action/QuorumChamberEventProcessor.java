package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class QuorumChamberEventProcessor extends EventProcessor<QuorumChamberEvent> {
    @Override
    public Followup processEvent() {
        val drawnCard = game.decks().quorum().draw();
        game.presidentHand().add(drawnCard);

        return one(
                new DrawQuorumCardEvent(event.playerNumber()),
                new PlayerDecisionEvent<>(event.playerNumber(), PlayQuorumCardEvent.class));
    }
}
