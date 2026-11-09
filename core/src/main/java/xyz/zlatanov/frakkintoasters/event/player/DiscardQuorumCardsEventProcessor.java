package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.ArrayList;

public class DiscardQuorumCardsEventProcessor extends EventProcessor<DiscardQuorumCardsEvent> {

    @Override
    protected boolean isValid() {
        return player.character() == game.president() && cardsInHand() && meetsDiscardLimit();
    }

    @Override
    public Followup process() {
        if (!event.cards().isEmpty()) {
            ops.titles().discardQuorumCards(event.cards());
        }
        return Followup.NONE;
    }

    // The Quorum deck can hold the same card twice, so each card in the event takes one copy out of the hand.
    private boolean cardsInHand() {
        val hand = new ArrayList<>(game.presidentHand().cards());
        return event.cards().stream().allMatch(hand::remove);
    }

    private boolean meetsDiscardLimit() {
        return switch (event.discardLimit()) {
            case null -> true;
            case DISCARD_DOWN_TO_HAND_LIMIT ->
                    game.presidentHand().size() - event.cards().size() == player.handLimit();
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
