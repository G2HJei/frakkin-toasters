package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Collection;

import static xyz.zlatanov.frakkintoasters.event.interrupt.CloseInterruptWindowEventProcessor.conflicts;

public class TiebreakInterruptCardsEventProcessor extends EventProcessor<TiebreakInterruptCardsEvent> {

    @Override
    protected boolean isValid() {
        val keptCards = event.keptCards();
        if (game.turn().interruptWindow().isEmpty() || keptCards == null) {
            return false;
        }
        val conflicts = conflicts(game);
        val onlyConflictingCardsKept = keptCards.stream()
                .allMatch(kept -> conflicts.stream().anyMatch(group -> group.contains(kept)));
        val oneCardKeptPerConflict = conflicts.stream()
                .allMatch(group -> group.stream().filter(keptCards::contains).count() == 1);
        return onlyConflictingCardsKept && oneCardKeptPerConflict;
    }

    @Override
    public Followup process() {
        conflicts(game).stream()
                .flatMap(Collection::stream)
                .filter(card -> !event.keptCards().contains(card))
                .forEach(card -> ops.interrupts().returnToHand(card));
        return Followup.NONE;
    }
}
