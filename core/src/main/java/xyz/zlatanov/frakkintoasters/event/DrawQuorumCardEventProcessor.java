package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

public class DrawQuorumCardEventProcessor extends EventProcessor<DrawQuorumCardEvent> {

    // The hand limit of 10 Quorum Cards applies at the end of the turn (rule 8.8). Nothing is drawn when every card is
    // in the President's hand, in play or out of the game.
    @Override
    public Followup process() {
        if (game.decks().quorumCardsLeft() > 0) {
            ops.titles().drawQuorumCard();
        }
        return Followup.NONE;
    }
}
