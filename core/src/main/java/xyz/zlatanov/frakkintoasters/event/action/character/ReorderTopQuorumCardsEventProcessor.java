package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class ReorderTopQuorumCardsEventProcessor extends EventProcessor<ReorderTopQuorumCardsEvent> {

    @Override
    protected boolean isValid() {
        if (event.order() == null || event.order().stream().anyMatch(Objects::isNull)) {
            return false;
        }
        val top = new ArrayList<>(InfluentialEventProcessor.top(game));
        val order = new ArrayList<>(event.order());
        top.sort(Comparator.comparing(QuorumCard::ordinal));
        order.sort(Comparator.comparing(QuorumCard::ordinal));
        return top.equals(order);
    }

    @Override
    public Followup process() {
        ops.titles().reorderTopQuorumCards(event.order());
        return NONE;
    }
}
