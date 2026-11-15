package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;

import java.util.Arrays;
import java.util.List;

// The current player keeps one card of each group of conflicting cards in the window, and the others go back to their
// owners' hands (rule 13.3).
public record TiebreakInterruptCardsEvent(List<PlayedInterruptCard> keptCards) implements Event {

    public TiebreakInterruptCardsEvent(PlayedInterruptCard... keptCards) {
        this(Arrays.stream(keptCards).toList());
    }
}
