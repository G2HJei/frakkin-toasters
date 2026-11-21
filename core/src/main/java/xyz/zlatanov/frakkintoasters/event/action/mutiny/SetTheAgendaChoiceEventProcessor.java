package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetTheAgendaChoiceEventProcessor extends EventProcessor<SetTheAgendaChoiceEvent> {

    static final int CARDS = 2;

    @Override
    protected boolean isValid() {
        return event.cards() != null
                && event.cards().stream().allMatch(Objects::nonNull)
                && event.cards().size() == Math.min(CARDS, game.presidentHand().size())
                && inHand(event.cards());
    }

    @Override
    public Followup process() {
        ops.titles().putQuorumCardsAtBottom(event.cards());
        return Followup.NONE;
    }

    // The hand can hold equal cards, so it counts copies.
    private boolean inHand(List<QuorumCard> cards) {
        val left = new ArrayList<>(game.presidentHand().cards());
        return cards.stream().allMatch(left::remove);
    }
}
