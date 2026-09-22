package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class QuorumChamberEventProcessor extends EventProcessor<QuorumChamberEvent> {

    // todo verify acting player is the president
    @Override
    public Followup process() {
        val drawnCard = game.decks().quorum().draw();
        game.presidentHand().addOnTop(drawnCard);

        return one(
                new DrawQuorumCardEvent(event.playerNumber()), // todo this should be player decision
                new PlayerDecisionEvent<>(event.playerNumber(), PlayQuorumCardEvent.class));
    }
}
